package com.gcplot.web.vertx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.Utils;
import com.gcplot.model.account.Account;
import com.gcplot.model.account.AccountImpl;
import com.gcplot.repository.AccountRepository;
import com.gcplot.web.Constants;
import com.gcplot.web.LoginInfo;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.impl.BodyHandlerImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.easymock.EasyMock.*;

public class VertxDispatcherTest {
    protected static final int TIMEOUT = 10;

    @Before
    public void setUp() throws Exception {
        vertx = Vertx.vertx();
        port = Utils.getFreePorts(1)[0];
        accountRepository = createMock(AccountRepository.class);
        dispatcher = new VertxDispatcher();
        dispatcher.setBodyHandler(new BodyHandlerImpl());
        dispatcher.setHost(HOST);
        dispatcher.setPort(port.value);
        dispatcher.setVertx(vertx);
        dispatcher.setAccountRepository(accountRepository);
        dispatcher.setMaxUploadSize(50*1024*1024);
        dispatcher.init();
    }

    @After
    public void tearDown() throws Exception {
        port.unlock();
        dispatcher.close();
        vertx.close();
    }

    @Test
    public void testBasic() throws Exception {
        CountDownLatch l = new CountDownLatch(2);
        final String message = "This is test message.";
        dispatcher.noAuth().get("/get/me", c -> {
            c.finish(message);
            l.countDown();
        });

        HttpClient client = vertx.createHttpClient();
        client.get(port.value, HOST, "/get/me", r -> {
            r.bodyHandler(b -> {
                if (new String(b.getBytes()).equals(message))
                    l.countDown();
            });
        }).end();

        Assert.assertTrue(l.await(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testNoAuth() throws Exception {
        final int[] count = { 0 };
        dispatcher.requireAuth().get("/get/me", c -> {
            count[0] += 1;
        });

        HttpClient client = vertx.createHttpClient();
        httpGet(client, "/get/me", jo -> jo.getLong("error") == ErrorMessages.NOT_AUTHORISED);
        Assert.assertEquals(count[0], 0);
    }

    @Test
    public void testAuth() throws Exception {
        final TestEntity response = new TestEntity(5, "VertxDisp", Arrays.asList(3, 4, 5));
        String token = UUID.randomUUID().toString();
        dispatcher.noAuth().get("/auth", c -> c.logIn(new LoginInfo(token, null)));
        dispatcher.requireAuth().get("/get/me", c -> c.response(response));

        HttpClient client = vertx.createHttpClient();
        CountDownLatch auth = new CountDownLatch(1);
        final String[] _token = { "" };
        client.get(port.value, HOST, "/auth", r -> {
            _token[0] = r.getHeader(Constants.AUTH_TOKEN_HEADER);
            auth.countDown();
        }).end();
        Assert.assertTrue(auth.await(TIMEOUT, TimeUnit.SECONDS));

        Account fakeAccount = new AccountImpl();
        expect(accountRepository.account(token)).andReturn(Optional.of(fakeAccount)).anyTimes();
        replay(accountRepository);

        CountDownLatch getMe = new CountDownLatch(1);
        client.get(port.value, HOST, "/get/me", c -> {
            c.bodyHandler(b -> {
                if (b.toString().equals("{\"result\":{\"num\":5,\"str\":\"VertxDisp\",\"nums\":[3,4,5]}}"))
                    getMe.countDown();
            });
        }).putHeader(Constants.AUTH_TOKEN_HEADER, _token[0]).end();

        Assert.assertTrue(getMe.await(TIMEOUT, TimeUnit.SECONDS));
    }

    protected void httpGet(HttpClient client, String path, Predicate<JsonObject> test) throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        client.get(port.value, HOST, path, r -> r.bodyHandler(b -> {
            JsonObject jo = new JsonObject(new String(b.getBytes()));
            if (test.test(jo)) {
                l.countDown();
            }
        })).end();
        Assert.assertTrue(l.await(TIMEOUT, TimeUnit.SECONDS));
    }

    protected Utils.Port port;
    protected AccountRepository accountRepository;
    protected VertxDispatcher dispatcher;
    protected Vertx vertx;
    protected static final String HOST = "127.0.0.1";


    public static class TestEntity {
        public final int num;
        public final String str;
        public final List<Integer> nums;

        @JsonCreator
        public TestEntity(@JsonProperty("num") int num, @JsonProperty("str") String str,
                          @JsonProperty("nums") List<Integer> nums) {
            this.num = num;
            this.str = str;
            this.nums = nums;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestEntity that = (TestEntity) o;
            return num == that.num &&
                    Objects.equals(str, that.str) &&
                    Objects.equals(nums, that.nums);
        }

        @Override
        public int hashCode() {
            return Objects.hash(num, str, nums);
        }
    }
}
