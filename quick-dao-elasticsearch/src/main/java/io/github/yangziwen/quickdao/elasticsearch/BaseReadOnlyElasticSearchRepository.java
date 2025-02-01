package io.github.yangziwen.quickdao.elasticsearch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequest.Item;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.alibaba.fastjson.JSON;

import io.github.yangziwen.quickdao.core.BaseReadOnlyRepository;
import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Criterion;
import io.github.yangziwen.quickdao.core.EntityMeta;
import io.github.yangziwen.quickdao.core.FunctionStmt;
import io.github.yangziwen.quickdao.core.Order;
import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.RepoKeys;
import io.github.yangziwen.quickdao.core.Stmt;
import io.github.yangziwen.quickdao.core.TypedCriteria;
import io.github.yangziwen.quickdao.core.TypedQuery;
import io.github.yangziwen.quickdao.core.util.ReflectionUtil;
import net.sf.cglib.beans.BeanMap;

public class BaseReadOnlyElasticSearchRepository<E> implements BaseReadOnlyRepository<E>  {

    private static final int DEFAULT_MAX_SIZE = 10000;

    protected final EntityMeta<E> entityMeta;

    protected final RestHighLevelClient client;

    protected final RequestOptions options;

    protected BaseReadOnlyElasticSearchRepository(RestHighLevelClient client) {
        this(client, RequestOptions.DEFAULT);
    }

    protected BaseReadOnlyElasticSearchRepository(RestHighLevelClient client, RequestOptions options) {
        this.entityMeta = EntityMeta.newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));
        this.client = client;
        this.options = options;
    }

    @Override
    public E getById(Object id) {
        try {
            GetRequest request = new GetRequest(entityMeta.getTable(), String.valueOf(id));
            GetResponse response = client.get(request, options);
            return extractEntityFromGetResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("failed to query entity of type " + entityMeta.getClassType().getName() + " by id " + id, e);
        }
    }

    @Override
    public List<E> listByIds(Collection<?> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        MultiGetRequest request = new MultiGetRequest();
        for (Object id : ids) {
            request.add(new Item(entityMeta.getTable(), String.valueOf(id)));
        }
        try {
            List<E> entities = new ArrayList<>(ids.size());
            MultiGetResponse response = client.mget(request, options);
            for (MultiGetItemResponse item : response.getResponses()) {
                entities.add(extractEntityFromGetResponse(item.getResponse()));
            }
            return entities;
        } catch (IOException e) {
            throw new RuntimeException("failed to query entity of type " + entityMeta.getClassType().getName() + " by ids " + ids, e);
        }

    }

    private E extractEntityFromGetResponse(GetResponse response) {
        if (!response.isExists()) {
            return null;
        }
        E entity = JSON.parseObject(response.getSourceAsString(), entityMeta.getClassType());
        Field idField = entityMeta.getIdField();
        if (idField != null) {
            BeanMap.create(entity).put(idField.getName(), response.getId());
        }
        return entity;
    }

    @Override
    public List<E> list(Query query) {
        boolean hasGroupBy = CollectionUtils.isNotEmpty(query.getGroupByList());
        boolean hasFuncStmt = query.getSelectStmtList().stream()
                .anyMatch(FunctionStmt.class::isInstance);
        if (!hasGroupBy && !hasFuncStmt) {
            return doListQuery(query);
        }
        else if (!hasGroupBy && hasFuncStmt) {
            return doListAggs(query);
        }
        else if (hasGroupBy) {
            return doListBucketAggs(query);
        }
        else {
            throw new RuntimeException("group by operation without func stmt is not supported, query is " + query);
        }
    }

    private List<E> doListAggs(Query query) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(generateQueryBuilder(query.getCriteria()))
                .from(0)
                .size(0);
        List<AggregationBuilder> aggsBuilderList = query.getSelectStmtList().stream()
                .filter(FunctionStmt.class::isInstance)
                .map(FunctionStmt.class::cast)
                .map(ElasticSearchFunctionEnum::generateAggsBuilder)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (AggregationBuilder aggsBuilder : aggsBuilderList) {
            sourceBuilder.aggregation(aggsBuilder);
        }
        SearchRequest request = new SearchRequest(entityMeta.getTable());
        request.source(sourceBuilder);
        try {
            SearchResponse response = client.search(request, options);
            Aggregations aggs = response.getAggregations();
            Map<String, Object> resultMap = aggs.asList()
                    .stream()
                    .collect(Collectors.toMap(
                            Aggregation::getName,
                            agg -> NumericMetricsAggregation.SingleValue.class.cast(agg).value()));
            E entity = JSON.parseObject(JSON.toJSONString(resultMap), entityMeta.getClassType());
            return Collections.singletonList(entity);
        } catch (IOException e) {
            throw new RuntimeException("failed to list aggregation of type " + entityMeta.getClassType().getName() + " by " + query, e);
        }
    }

    private List<E> doListBucketAggs(Query query) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(generateQueryBuilder(query.getCriteria()))
                .from(0)
                .size(0);

        @SuppressWarnings("rawtypes")
        List<FunctionStmt> funcStmtList = query.getSelectStmtList().stream()
                .filter(FunctionStmt.class::isInstance)
                .map(FunctionStmt.class::cast)
                .collect(Collectors.toList());

        AggregationBuilder outterAggsBuilder = null;
        AggregationBuilder innerAggsBuilder = null;
        Map<String, TermsAggregationBuilder> termsAggsBuilderMap = new HashMap<>();
        for (String groupBy : query.getGroupByList()) {
            TermsAggregationBuilder aggsBuilder = AggregationBuilders.terms(groupBy.replaceFirst("\\.keyword$", "")).field(groupBy);
            // 注意桶聚合时，无法精确控制结果的总数量，只能控制每级聚合结果的数量
            aggsBuilder.size(validateLimit(query.getLimit()));
            termsAggsBuilderMap.put(groupBy, aggsBuilder);
            if (outterAggsBuilder == null) {
                outterAggsBuilder = aggsBuilder;
            }
            if (innerAggsBuilder != null) {
                innerAggsBuilder.subAggregation(aggsBuilder);
            }
            innerAggsBuilder = aggsBuilder;
        }

        List<AggregationBuilder> statsAggsBuilderList = funcStmtList.stream()
                .map(ElasticSearchFunctionEnum::generateAggsBuilder)
                .collect(Collectors.toList());
        for (AggregationBuilder statsAggs : statsAggsBuilderList) {
            innerAggsBuilder.subAggregation(statsAggs);
        }

        if (CollectionUtils.isNotEmpty(query.getOrderList())) {
            for (Order order : query.getOrderList()) {
                TermsAggregationBuilder termsAggsBuilder = termsAggsBuilderMap.get(order.getName());
                if (termsAggsBuilder != null) {
                    termsAggsBuilder.order(BucketOrder.key(order.getDirection() == Direction.ASC));
                }
            }
        }

        sourceBuilder.aggregation(outterAggsBuilder);

        SearchRequest request = new SearchRequest(entityMeta.getTable());

        request.source(sourceBuilder);

        try {
            SearchResponse response = client.search(request, options);
            List<Map<String, Object>> resultList = walkAggregations(response.getAggregations());
            List<E> entities = new ArrayList<>(resultList.size());
            for (Map<String, Object> result : resultList) {
                entities.add(JSON.parseObject(JSON.toJSONString(result), entityMeta.getClassType()));
            }
            return entities;
        } catch (IOException e) {
            throw new RuntimeException("failed to list aggregation of type " + entityMeta.getClassType().getName() + " by " + query, e);
        }
    }

    private List<Map<String, Object>> walkAggregations(Aggregations aggregations) {
        if (aggregations == null) {
            return Collections.emptyList();
        }
        List<Aggregation> aggregationList = aggregations.asList();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Aggregation aggregation : aggregationList) {
            if (aggregation instanceof ParsedTerms) {
                String key = aggregation.getName();
                List<? extends Terms.Bucket> bucketList = ParsedTerms.class.cast(aggregation).getBuckets();
                for (Terms.Bucket bucket : bucketList) {
                    Object value = bucket.getKey();
                    List<Map<String, Object>> mapList = walkAggregations(bucket.getAggregations());
                    for (Map<String, Object> map : mapList) {
                        map.put(key, value);
                        map.putIfAbsent("count", bucket.getDocCount());
                    }
                    resultList.addAll(mapList);
                }
            } else {
                break;
            }
        }
        Map<String, Object> result = new HashMap<>();
        for (Aggregation aggregation : aggregationList) {
            if (aggregation instanceof NumericMetricsAggregation.SingleValue) {
                SingleValue valueObj = NumericMetricsAggregation.SingleValue.class.cast(aggregation);
                result.put(valueObj.getName(), valueObj.value());
            } else {
                break;
            }
        }
        if (MapUtils.isNotEmpty(result)) {
            resultList.add(result);
        }
        return resultList;
    }

    private List<E> doListQuery(Query query) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(generateQueryBuilder(query.getCriteria()))
                .from(query.getOffset())
                .size(validateLimit(query.getLimit()));
        List<Stmt> stmtList = query.getSelectStmtList()
                .stream()
                .filter(stmt -> !FunctionStmt.class.isInstance(stmt))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(stmtList)) {
            List<String> includes = stmtList.stream()
                    .map(Stmt::getField)
                    .collect(Collectors.toList());
            sourceBuilder.fetchSource(includes.toArray(ArrayUtils.EMPTY_STRING_ARRAY), ArrayUtils.EMPTY_STRING_ARRAY);
        }
        if (CollectionUtils.isNotEmpty(query.getOrderList())) {
            for (Order order : query.getOrderList()) {
                sourceBuilder.sort(generateSortBuilder(order));
            }
        }
        SearchRequest request = new SearchRequest(entityMeta.getTable());
        request.source(sourceBuilder);
        try {
            List<E> entities = new ArrayList<>();
            SearchResponse response = client.search(request, options);
            for (SearchHit hit : response.getHits().getHits()) {
                E entity = JSON.parseObject(hit.getSourceAsString(), entityMeta.getClassType());
                Field idField = entityMeta.getIdField();
                if (idField != null) {
                    BeanMap.create(entity).put(idField.getName(), hit.getId());
                }
                entities.add(entity);
            }
            return entities;
        } catch (IOException e) {
            throw new RuntimeException("failed to list entity of type " + entityMeta.getClassType().getName() + " by " + query, e);
        }
    }

    @Override
    public Integer count(Query query) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(generateQueryBuilder(query.getCriteria()));
        CountRequest request = new CountRequest(entityMeta.getTable());
        request.source(sourceBuilder);
        try {
            CountResponse response = client.count(request, options);
            return Long.valueOf(response.getCount()).intValue();
        } catch (IOException e) {
            throw new RuntimeException("failed to count entity of type " + entityMeta.getClassType().getName() + " by " + query, e);
        }
    }

    private SortBuilder<?> generateSortBuilder(Order order) {
        SortOrder sortOrder = SortOrder.ASC;
        if (order.getDirection() == Direction.DESC) {
            sortOrder = SortOrder.DESC;
        }
        return SortBuilders.fieldSort(order.getName()).order(sortOrder);
    }

    protected QueryBuilder generateQueryBuilder(Criteria criteria) {
        if (criteria.isEmpty()) {
            return QueryBuilders.matchAllQuery();
        }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Criterion<?> criterion : criteria.getCriterionList()) {
            ElasticSearchOperator operator = ElasticSearchOperator.from(criterion.getOperator());
            if (operator == null) {
                continue;
            }
            boolQueryBuilder.must(operator.generateQueryBuilder(criterion));
        }
        for (Entry<String, Criteria> entry : criteria.getNestedCriteriaMap().entrySet()) {
            if (entry.getKey().endsWith(RepoKeys.AND)) {
                boolQueryBuilder.must(generateQueryBuilder(entry.getValue()));
            }
            if (entry.getKey().endsWith(RepoKeys.OR)) {
                boolQueryBuilder.should(generateQueryBuilder(entry.getValue()));
            }
        }
        return boolQueryBuilder;
    }

    @Override
    public TypedCriteria<E> newTypedCriteria() {
        return new TypedCriteria<>(entityMeta.getClassType());
    }

    @Override
    public TypedQuery<E> newTypedQuery() {
        return new TypedQuery<>(entityMeta.getClassType());
    }

    @SuppressWarnings("unchecked")
    protected <K, V> Map<K, V> createBeanMap(E entity) {
        if (entity == null) {
            return Collections.emptyMap();
        }
        return BeanMap.create(entity);
    }

    protected int validateLimit(int limit) {
        if (limit == Integer.MAX_VALUE) {
            return DEFAULT_MAX_SIZE;
        }
        if (limit > DEFAULT_MAX_SIZE) {
            throw new RuntimeException("limit cannot exceed 10000");
        }
        return limit;
    }

}
