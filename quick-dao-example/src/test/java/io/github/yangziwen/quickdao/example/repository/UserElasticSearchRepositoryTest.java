package io.github.yangziwen.quickdao.example.repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.Page;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.enums.Gender;

public class UserElasticSearchRepositoryTest {

    private static RestHighLevelClient client;

    private static UserElasticSearchRepository repository;

    @BeforeClass
    public static void beforeClass() throws Exception {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
        repository = new UserElasticSearchRepository(client);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void testGetById() {
        User expectedUser = repository.first(new Criteria());
        User user = repository.getById(expectedUser.getId());
        Assert.assertEquals(expectedUser.getId(), user.getId());
    }

    @Test
    public void testFirst() {
        Integer maxAge = repository.list().stream()
                .mapToInt(User::getAge)
                .max()
                .getAsInt();
        User user = repository.first(new Query().orderBy("age", Direction.DESC));
        Assert.assertEquals(maxAge, user.getAge());
    }

    @Test
    public void testListByIds() {
        List<Object> idList = repository.list(new Query().limit(2)).stream()
                .map(User::getId)
                .collect(Collectors.toList());
        List<User> userList = repository.listByIds(idList);
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListByQuery() {
        Criteria criteria = new Criteria()
                .and("username").startWith("张")
                .and("gender").eq(Gender.FEMALE)
                .and("city").eq("上海")
                .and("createTime").lt(new Date());
        Query query = new Query().where(criteria);
        List<User> userList = repository.list(query);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithQueryFromQueryMap() {
        Criteria criteria = new Criteria()
                .and("username").startWith("张")
                .and("gender").eq(Gender.FEMALE)
                .and("city").eq("上海")
                .and("createTime").lt(new Date());
        criteria = Criteria.fromParamMap(criteria.toParamMap());
        List<User> userList = repository.list(criteria);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithOrQuery() {
        Criteria criteria = new Criteria()
                .or()
                    .and("username").eq("王四")
                    .and("gender").eq(Gender.FEMALE)
                    .and("age").eq(25)
                    .and("city").eq("天津")
                .end()
                .or()
                    .and("username").eq("张三")
                    .and("gender").eq(Gender.MALE)
                    .and("age").eq(26)
                    .and("city").eq("南京")
                .end();
        Query query = new Query().where(criteria).limit(2);
        List<User> userList = repository.list(query);
        Assert.assertEquals(2, userList.size());
    }

    @Test
    public void testListContain() {
        Query query = new Query().where(new Criteria()
                .and("username").contain("张")
                .and("username").contain("三"));
        List<User> userList = repository.list(query);
        Assert.assertEquals(1, userList.size());
        Assert.assertEquals("张三", userList.get(0).getUsername());
    }

    @Test
    public void testListNotContain() {
        Query query = new Query().where(new Criteria()
                .and("username").notContain("张")
                .and("username").notContain("王")
                .and("username").notContain("李"));
        List<User> userList = repository.list(query);
        Assert.assertEquals(5, userList.size());
        for (User user : userList) {
            Assert.assertTrue(user.getUsername().startsWith("赵"));
        }
    }

    @Test
    public void testListIsNull() {
        Query query = new Query().where(new Criteria()
                .and("username").eq("张三")
                .and("email").isNull());
        List<User> userList = repository.list(query);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListIsNotNull() {
        Query query = new Query().where(new Criteria()
                .and("username").eq("张三")
                .and("age").isNotNull());
        List<User> userList = repository.list(query);
        Assert.assertEquals(1, userList.size());
    }

    @Test
    public void testListWithGroup() {
        Query query = new Query()
                .where(new Criteria().and("username").startWith("张"))
                .groupBy("city");
        List<User> userList = repository.list(query);
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
        Query query = new Query()
                .where(new Criteria().and("username").startWith("张"))
                .orderBy("age")
                .offset(2)
                .limit(10);
        List<User> userList = repository.list(query);
        Assert.assertEquals(3, userList.size());
        for (User user : userList) {
            Assert.assertTrue(user.getAge() > 25);
        }
    }

    @Test
    public void testCount() {
        Assert.assertEquals(Integer.valueOf(20), repository.count());
        Assert.assertEquals(Integer.valueOf(5), repository.count(new Criteria()
                .and("username").startWith("张")));
    }

    @Test
    public void testPaginate() {
        Query query = new Query()
                .where(new Criteria().and("username").startWith("张"))
                .orderBy("username");
        Page<User> userPage = repository.paginate(query, 1, 3);
        Assert.assertEquals(3, userPage.getList().size());
        Assert.assertEquals(5, userPage.getTotalCount().intValue());
    }

}
