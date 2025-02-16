package io.github.yangziwen.quickdao.example.repository;

import java.util.Date;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.Page;
import io.github.yangziwen.quickdao.core.TypedCriteria;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.enums.Gender;
import io.github.yangziwen.quickdao.example.repository.helper.UserElasticSearchHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserTypedCriteriaElasticSearchRepositoryTest {

    private static ElasticsearchContainer container;

    private static RestHighLevelClient client;

    private static UserElasticSearchRepository repository;

    @BeforeClass
    public static void beforeClass() throws Exception {
        container = UserElasticSearchHelper.startNewContainer();
        log.info("container is ready");

        client = new RestHighLevelClient(RestClient.builder(new HttpHost(
                container.getHost(),
                container.getMappedPort(9200),
                "http")));
        log.info("client is ready");

        repository = new UserElasticSearchRepository(client);
        UserElasticSearchHelper.prepareData(repository);
        Thread.sleep(1000L);
        log.info("repository is ready");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (client != null) {
            client.close();
            log.info("client is closed");
        }
        if (container != null) {
            container.stop();
            log.info("container is closed");
        }
    }

    @Test
    public void testListWithQuery() {
        List<User> userList = repository.listQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getUsername).startWith("张")
                        .and(User::getGender).eq(Gender.FEMALE)
                        .and(User::getCity).eq("上海")
                        .and(User::getCreateTime).lt(new Date())));
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithQueryFromQueryMap() {
        TypedCriteria<User> criteria = repository.newTypedCriteria()
                .and(User::getUsername).startWith("张")
                .and(User::getGender).eq(Gender.FEMALE)
                .and(User::getCity).eq("上海")
                .and(User::getCreateTime).lt(new Date());
        criteria = TypedCriteria.fromParamMap(User.class, criteria.toParamMap());
        List<User> userList = repository.list(criteria);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithOrQuery() {
        List<User> userList = repository.listQuery(query -> query
                .select(User::getId)
                .select(User::getUsername)
                .select(User::getAge)
                .select("createTime").as(User::getCreateTime)
                .where(criteria -> criteria
                        .or()
                            .and(User::getUsername).eq("王四")
                            .and(User::getGender).eq(Gender.FEMALE)
                            .and(User::getAge).eq(25)
                            .and(User::getCity).eq("天津")
                        .end()
                        .or()
                            .and(User::getUsername).eq("张三")
                            .and(User::getGender).eq(Gender.MALE)
                            .and(User::getAge).eq(26)
                            .and(User::getCity).eq("南京")
                        .end())
                .orderBy(User::getCity, Direction.DESC)
                .limit(2));
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListWithOrQueryFromQueryMap() {
        TypedCriteria<User> criteria = repository.newTypedCriteria()
                .or()
                    .and(User::getUsername).eq("王四")
                    .and(User::getGender).eq(Gender.FEMALE)
                    .and(User::getAge).eq(25)
                    .and(User::getCity).eq("天津")
                .end()
                .or()
                    .and(User::getUsername).eq("张三")
                    .and(User::getGender).eq(Gender.MALE)
                    .and(User::getAge).eq(26)
                    .and(User::getCity).eq("南京")
                .end();
        List<User> userList = repository.listQuery(query -> query
                .where(TypedCriteria.fromParamMap(User.class, criteria.toParamMap()))
                .orderBy(User::getCity, Direction.DESC)
                .limit(2));
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListContain() {
        List<User> userList = repository.listQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getUsername).contain("张")
                        .and(User::getUsername).contain("三")));
        Assert.assertEquals(1, userList.size());
        Assert.assertEquals("张三", userList.get(0).getUsername());
    }

    @Test
    public void testListNotContain() {
        List<User> userList = repository.listQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getUsername).notContain("张")
                        .and(User::getUsername).notContain("王")
                        .and(User::getUsername).notContain("李")));
        Assert.assertEquals(5, userList.size());
        for (User user : userList) {
            Assert.assertTrue(user.getUsername().startsWith("赵"));
        }
    }

    @Test
    public void testListIsNull() {
        List<User> userList = repository.listQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getUsername).eq("张三")
                        .and(User::getEmail).isNull()));
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListIsNotNull() {
        List<User> userList = repository.listQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getUsername).eq("张三")
                        .and(User::getEmail).isNotNull()));
        Assert.assertEquals(0, userList.size());
    }

    @Test
    public void testListWithGruop() {
        List<User> userList = repository.listQuery(query -> query
                .selectExpr(expr -> expr.max(User::getAge)).as(User::getMaxAge)
                .selectExpr(expr -> expr.min(User::getAge)).as(User::getMinAge)
                .selectExpr(expr -> expr.avg(User::getAge)).as(User::getAvgAge)
                .selectExpr(expr -> expr.countDistinct(User::getAge)).as(User::getDistinctCount)
                .select(User::getCity)
                .select(User::getCount)
                .where(criteria -> criteria
                        .and(User::getUsername).startWith("张"))
                .groupBy(User::getCity)
                .orderBy(User::getCity));
        Assert.assertEquals(3, userList.size());
        for (User user : userList) {
            if (user.getCity().equals("上海")) {
                Assert.assertEquals(Integer.valueOf(2), user.getCount());
            }
            if (user.getCity().equals("北京")) {
                Assert.assertEquals(Integer.valueOf(2), user.getCount());
            }
            if (user.getCity().equals("南京")) {
                Assert.assertEquals(Integer.valueOf(1), user.getCount());
            }
        }
    }

    @Test
    public void testListWithLimit() {
        List<User> userList = repository.listQuery(query -> query
                .where(criteria -> criteria
                        .and(User::getUsername).startWith("张"))
                .orderBy(User::getAge)
                .offset(2)
                .limit(10));
        Assert.assertEquals(3, userList.size());
        for (User user : userList) {
            Assert.assertTrue(user.getAge() > 25);
        }
    }

    @Test
    public void testCount() {
        Assert.assertEquals(Integer.valueOf(5), repository
                .countCriteria(criteria -> criteria
                        .and("username").startWith("张")));
    }

    @Test
    public void testPaginate() {
        Page<User> userPage = repository.paginateCriteria(criteria -> criteria
                .and("username").startWith("张"), 1, 3);
        Assert.assertEquals(3, userPage.getList().size());
        Assert.assertEquals(5, userPage.getTotalCount().intValue());
    }

}
