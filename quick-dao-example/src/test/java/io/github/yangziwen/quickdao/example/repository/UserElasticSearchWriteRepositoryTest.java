package io.github.yangziwen.quickdao.example.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.enums.Gender;
import io.github.yangziwen.quickdao.example.repository.helper.UserElasticSearchHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * BaseElasticSearchRepository 写路径（insert/update/delete）的单测。
 * 每个用例只操作自己插入的文档（使用各用例唯一的 city），与用例执行顺序无关；
 * 走 search 通道的验证（count / deleteByQuery）需等待 ES refresh。
 */
@Slf4j
public class UserElasticSearchWriteRepositoryTest {

    private static ElasticsearchContainer container;

    private static RestHighLevelClient client;

    private static UserElasticSearchRepository repository;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Assume.assumeTrue("docker is not available", DockerClientFactory.instance().isDockerAvailable());
        container = UserElasticSearchHelper.startNewContainer();
        log.info("container is ready");

        client = new RestHighLevelClient(RestClient.builder(new HttpHost(
                container.getHost(),
                container.getMappedPort(9200),
                "http")));

        repository = new UserElasticSearchRepository(client);
        UserElasticSearchHelper.prepareData(repository);
        Thread.sleep(1000L);
        log.info("repository is ready");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (client != null) {
            client.close();
        }
        if (container != null) {
            container.stop();
        }
    }

    private User newUser(String city, String username, Integer age) {
        return User.builder()
                .username(username)
                .gender(Gender.MALE)
                .city(city)
                .age(age)
                .createTime(new Date())
                .build();
    }

    /**
     * search 通道（count）只能看到已 refresh 的 segment（默认 refresh_interval=1s），
     * 写入后轮询等待直到命中期望值或超时，避免固定 sleep 的保守与偶发不足。
     */
    private void awaitCount(Criteria criteria, int expected) throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) {
            if (repository.count(criteria) == expected) {
                return;
            }
            Thread.sleep(200L);
        }
        Assert.assertEquals(expected, repository.count(criteria).intValue());
    }

    @Test
    public void testInsertBackfillsGeneratedId() {
        User user = newUser("写测市A", "写测一", 30);
        Assert.assertEquals(1, repository.insert(user));
        // @GeneratedValue 生效：ES 生成的 id 应回填到实体
        Assert.assertNotNull(user.getId());
        User loaded = repository.getById(user.getId());
        Assert.assertEquals("写测一", loaded.getUsername());
        Assert.assertEquals(Integer.valueOf(30), loaded.getAge());
        Assert.assertEquals("写测市A", loaded.getCity());
    }

    @Test
    public void testBatchInsertEdgeCases() {
        Assert.assertEquals(0, repository.batchInsert(new ArrayList<>(), 10));
        Assert.assertEquals(0, repository.batchInsert(null, 10));
        try {
            repository.batchInsert(Collections.singletonList(newUser("写测市B", "写测二", 1)), 0);
            Assert.fail("batchSize <= 0 should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testUpdate() {
        User user = newUser("写测市C", "写测三", 20);
        repository.insert(user);

        user.setAge(21);
        Assert.assertEquals(1, repository.update(user));
        // getById 走 GetRequest（realtime 读），无需等待 refresh
        Assert.assertEquals(Integer.valueOf(21), repository.getById(user.getId()).getAge());

        try {
            repository.update(new User());
            Assert.fail("update without id should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testUpdateSelective() {
        User user = newUser("写测市D", "写测四原名", 20);
        repository.insert(user);

        User partial = new User();
        partial.setId(user.getId());
        partial.setAge(22);
        Assert.assertEquals(1, repository.updateSelective(partial));
        User loaded = repository.getById(user.getId());
        Assert.assertEquals(Integer.valueOf(22), loaded.getAge());
        // 未指定的字段不受影响
        Assert.assertEquals("写测四原名", loaded.getUsername());

        // 所有字段均为 null 时无可更新内容
        User empty = new User();
        empty.setId(user.getId());
        Assert.assertEquals(0, repository.updateSelective(empty));
    }

    @Test
    public void testUpdateSelectiveByCriteria() {
        String city = "写测市E";
        User u1 = newUser(city, "写测五", 10);
        User u2 = newUser(city, "写测六", 20);
        repository.insert(u1);
        repository.insert(u2);

        User template = new User();
        template.setAge(99);
        Criteria criteria = new Criteria().and("city").eq(city);
        // update_by_query 走 search 通道，需等待新文档 refresh 后才可见
        awaitCount(criteria, 2);
        Assert.assertEquals(2, repository.updateSelective(template, criteria));
        Assert.assertEquals(Integer.valueOf(99), repository.getById(u1.getId()).getAge());
        Assert.assertEquals(Integer.valueOf(99), repository.getById(u2.getId()).getAge());

        // 空 criteria 会追加 id is null 条件，不应更新任何文档
        Assert.assertEquals(0, repository.updateSelective(template, new Criteria()));
    }

    @Test
    public void testDeleteById() {
        User user = newUser("写测市F", "写测七", 30);
        repository.insert(user);
        Assert.assertNotNull(repository.getById(user.getId()));

        Assert.assertEquals(1, repository.deleteById(user.getId()));
        Assert.assertNull(repository.getById(user.getId()));

        Assert.assertEquals(0, repository.deleteById(null));
        Assert.assertEquals(0, repository.deleteById("不存在的id"));
    }

    @Test
    public void testDeleteByIds() {
        User u1 = newUser("写测市G", "写测八", 30);
        User u2 = newUser("写测市G", "写测九", 31);
        repository.insert(u1);
        repository.insert(u2);

        Assert.assertEquals(2, repository.deleteByIds(Arrays.asList(u1.getId(), u2.getId())));
        Assert.assertNull(repository.getById(u1.getId()));
        Assert.assertNull(repository.getById(u2.getId()));

        Assert.assertEquals(0, repository.deleteByIds(new ArrayList<>()));
        // 文档不存在视为未删除，不算失败
        Assert.assertEquals(0, repository.deleteByIds(Collections.singletonList("不存在的id")));
    }

    @Test
    public void testDeleteByCriteriaAndQuery() throws Exception {
        String city = "写测市H";
        for (int i = 0; i < 3; i++) {
            repository.insert(newUser(city, "写测十" + i, 40 + i));
        }
        awaitCount(new Criteria().and("city").eq(city), 3);

        Assert.assertEquals(3, repository.delete(new Criteria().and("city").eq(city)));
        awaitCount(new Criteria().and("city").eq(city), 0);

        // delete(Query) 委托给 delete(Criteria)
        repository.insert(newUser(city, "写测十一", 50));
        awaitCount(new Criteria().and("city").eq(city), 1);
        Assert.assertEquals(1, repository.delete(new Query().where(new Criteria().and("city").eq(city))));
        awaitCount(new Criteria().and("city").eq(city), 0);
    }

}
