package io.github.yangziwen.quickdao.elasticsearch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.action.DocWriteRequest.OpType;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import net.sf.cglib.beans.BeanMap;

public abstract class BaseElasticSearchRepository<E> extends BaseReadOnlyElasticSearchRepository<E> implements BaseRepository<E> {

    protected BaseElasticSearchRepository(RestHighLevelClient client) {
        super(client);
    }

    protected BaseElasticSearchRepository(RestHighLevelClient client, RequestOptions options) {
        super(client, options);
    }

    @Override
    public int insert(E entity) {

        Map<String, Object> beanMap = createBeanMap(entity);

        IndexRequest request = generateIndexRequest(beanMap);

        try {
            IndexResponse response = client.index(request, options);
            if (entityMeta.getIdField() != null && entityMeta.getIdGeneratedValue() != null) {
                beanMap.put(entityMeta.getIdFieldName(), response.getId());
            }
            return response.getResult() == Result.CREATED ? 1 : 0;
        } catch (IOException e) {
           throw new PersistenceException("failed to persist entity of type " + entityMeta.getClassType().getName(), e);
        }
    }

    @Override
    public int batchInsert(List<E> entities, int batchSize) {

        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }

        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }

        int affectedRows = 0;

        List<String> failureMessageList = new ArrayList<>();

        boolean needToBackfillId = entityMeta.getIdField() != null && entityMeta.getIdGeneratedValue() != null;

        for (int i = 0; i < entities.size(); i += batchSize) {
            List<E> sublist = entities.subList(i, Math.min(i + batchSize, entities.size()));
            List<BeanMap> beanMapList = sublist.stream()
                    .map(BeanMap::create)
                    .collect(Collectors.toList());
            BulkRequest request = new BulkRequest();
            for (BeanMap beanMap : beanMapList) {
                request.add(generateIndexRequest(beanMap));
            }
            try {
                BulkResponse response = client.bulk(request, options);
                for (int j = 0; j < response.getItems().length; j++) {
                    BulkItemResponse item = response.getItems()[j];
                    if (item.isFailed()) {
                        failureMessageList.add(String.format("insert of index[%s] with id[%s] failed: %s",
                                item.getIndex(), item.getId(), item.getFailureMessage()));
                        continue;
                    }
                    affectedRows++;
                    if (needToBackfillId) {
                        beanMapList.get(j).put(entityMeta.getIdFieldName(), item.getId());
                    }
                }
            } catch (IOException e) {
                throw new PersistenceException("failed to persist entities of type " + entityMeta.getClassType().getName(), e);
            }
        }

        // 部分失败时显式抛出异常，避免静默丢数据
        if (CollectionUtils.isNotEmpty(failureMessageList)) {
            throw new PersistenceException("failed to persist entities of type " + entityMeta.getClassType().getName()
                    + ", " + String.join("; ", failureMessageList));
        }

        return affectedRows;
    }

    private IndexRequest generateIndexRequest(Map<String, Object> beanMap) {

        Map<String, Object> entityMap = new HashMap<>();

        for (Field field : entityMeta.getFieldsWithoutIdField()) {
            entityMap.put(field.getName(), beanMap.get(field.getName()));
        }

        IndexRequest request = new IndexRequest(entityMeta.getTable());

        if (entityMeta.getIdField() != null && entityMeta.getIdGeneratedValue() == null) {
            Object idVal = beanMap.get(entityMeta.getIdField().getName());
            if (idVal == null) {
                throw new IllegalStateException("failed to get id of entity[" + beanMap + "]");
            }
            request.id(String.valueOf(idVal));
            request.opType(OpType.CREATE);
        }

        request.source(entityMap);

        return request;
    }

    @Override
    public int update(E entity) {

        Map<String, Object> beanMap = createBeanMap(entity);

        Map<String, Object> entityMap = new HashMap<>();

        for (Field field : entityMeta.getFieldsWithoutIdField()) {
            entityMap.put(field.getName(), beanMap.get(field.getName()));
        }

        Object idVal = beanMap.get(entityMeta.getIdFieldName());

        if (idVal == null) {
            throw new IllegalStateException("failed to get id of entity[" + beanMap + "]");
        }

        UpdateRequest request = new UpdateRequest(entityMeta.getTable(), String.valueOf(idVal));

        request.doc(entityMap);

        try {
            UpdateResponse response = client.update(request, options);
            return response.getResult() == Result.UPDATED ? 1 : 0;
        } catch (IOException e) {
            throw new PersistenceException("failed to update entity of type " + entityMeta.getClassType().getName(), e);
        }
    }

    @Override
    public int updateSelective(E entity) {

        Map<String, Object> beanMap = createBeanMap(entity);

        Map<String, Object> entityMap = new HashMap<>();

        for (Field field : entityMeta.getFieldsWithoutIdField()) {
            Object value = beanMap.get(field.getName());
            if (value == null) {
                continue;
            }
            entityMap.put(field.getName(), value);
        }

        if (entityMap.isEmpty()) {
            return 0;
        }

        Object idVal = beanMap.get(entityMeta.getIdFieldName());

        if (idVal == null) {
            throw new IllegalStateException("failed to get id of entity[" + beanMap + "]");
        }

        UpdateRequest request = new UpdateRequest(entityMeta.getTable(), String.valueOf(idVal));

        request.doc(entityMap);

        try {
            UpdateResponse response = client.update(request, options);
            return response.getResult() == Result.UPDATED ? 1 : 0;
        } catch (IOException e) {
            throw new PersistenceException("failed to update entity of type " + entityMeta.getClassType().getName(), e);
        }

    }

    @Override
    public int updateSelective(E entity, Criteria crieria) {

        Map<String, Object> beanMap = createBeanMap(entity);

        List<String> assignLineList = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();

        // 只更新非空字段，且不修改 id 字段
        for (Field field : entityMeta.getFieldsWithoutIdField()) {
            Object value = beanMap.get(field.getName());
            if (value == null) {
                continue;
            }
            assignLineList.add(String.format("ctx._source.%s = params.%s", field.getName(), field.getName()));
            params.put(field.getName(), value);
        }

        if (assignLineList.isEmpty()) {
            return 0;
        }

        Script script = new Script(ScriptType.INLINE, "painless", StringUtils.join(assignLineList, ";"), params);

        // 与 SQL 版 generateUpdateSelectiveByCriteriaSql 保持一致：
        // 空 criteria 时追加 id is null 条件，避免 match_all 导致全量更新
        if (crieria.isEmpty()) {
            crieria.and(entityMeta.getIdFieldName()).isNull();
        }

        UpdateByQueryRequest request = new UpdateByQueryRequest(entityMeta.getTable());

        request.setQuery(generateQueryBuilder(crieria));

        request.setScript(script);

        try {
            BulkByScrollResponse response = client.updateByQuery(request, options);
            return Long.valueOf(response.getUpdated()).intValue();
        } catch (IOException e) {
            throw new PersistenceException("failed to update entity of type " + entityMeta.getClassType().getName(), e);
        }
    }

    @Override
    public int deleteById(Object id) {

        if (id == null) {
            return 0;
        }

        DeleteRequest request = new DeleteRequest(entityMeta.getTable(), String.valueOf(id));

        DeleteResponse response;
        try {
            response = client.delete(request, options);
            return response.getResult() == Result.DELETED ? 1 : 0;
        } catch (IOException e) {
            throw new PersistenceException("failed to delete entity of type " + entityMeta.getClassType().getName(), e);
        }
    }

    @Override
    public int deleteByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        BulkRequest request = new BulkRequest();
        for (Object id : ids) {
            request.add(new DeleteRequest(entityMeta.getTable(), String.valueOf(id)));
        }
        try {
            int result = ids.size();

            List<String> failureMessageList = new ArrayList<>();

            BulkResponse response = client.bulk(request, options);
            for (BulkItemResponse item : response.getItems()) {
                if (!item.isFailed()) {
                    continue;
                }
                // 文档不存在视为未删除，与 SQL 语义一致，不算失败
                if (item.getFailure().getStatus() == RestStatus.NOT_FOUND) {
                    result--;
                    continue;
                }
                failureMessageList.add(String.format("delete of index[%s] with id[%s] failed: %s",
                        item.getIndex(), item.getId(), item.getFailureMessage()));
            }

            // 非文档缺失的失败需要显式抛出，避免静默丢数据
            if (CollectionUtils.isNotEmpty(failureMessageList)) {
                throw new PersistenceException("failed to delete entities of type " + entityMeta.getClassType().getName()
                        + ", " + String.join("; ", failureMessageList));
            }

            return result;
        } catch (IOException e) {
            throw new PersistenceException("failed to delete entities of type " + entityMeta.getClassType().getName(), e);
        }
    }

    @Override
    public int delete(Criteria criteria) {

        DeleteByQueryRequest request = new DeleteByQueryRequest(entityMeta.getTable());

        request.setQuery(generateQueryBuilder(criteria));

        try {
            BulkByScrollResponse response = client.deleteByQuery(request, options);
            return Long.valueOf(response.getDeleted()).intValue();
        } catch (IOException e) {
            throw new PersistenceException("failed to delete entities of type " + entityMeta.getClassType().getName(), e);
        }
    }

    @Override
    public int delete(Query query) {
        // be aware that this method will ignore "order by", "offset" and "limit"
        return delete(query.getCriteria());
    }

}
