package com.gcplot.model.orientdb;

import com.gcplot.model.Account;
import com.gcplot.model.AccountImpl;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OrientDbRepositoryTest {

    protected OrientDbConfig config;

    @Before
    public void setUp() throws Exception {
        new ODatabaseDocumentTx("memory:test").create();
        config = new OrientDbConfig("memory:test", "admin", "admin");
    }

    @After
    public void tearDown() throws Exception {
        new ODatabaseDocumentTx("memory:test").open("admin", "admin").drop();
    }

    @Test
    public void test() throws Exception {
        OrientDbRepository repository = new OrientDbRepository(config, new OPartitionedDatabasePoolFactory());
        repository.init();
        Assert.assertFalse(repository.account("token").isPresent());
        AccountImpl account = AccountImpl.createNew("abc", "artem@reveno.org", "token", "pass", "salt");
        account = (AccountImpl) repository.store(account);
        Assert.assertNotNull(account.getOId());
        Assert.assertTrue(repository.account("token").isPresent());
        Assert.assertFalse(repository.account("token").get().isConfirmed());
        Assert.assertTrue(repository.confirm("token", "salt"));
        Account account1 = repository.account("token").get();
        Assert.assertTrue(account1.isConfirmed());

        Assert.assertEquals(repository.accounts().size(), 1);
        Assert.assertNotNull(repository.account(account1.id()).get());

        Assert.assertTrue(repository.account("abc", "pass").isPresent());
        repository.delete(account1);
        Assert.assertEquals(repository.accounts().size(), 0);

        repository.destroy();
    }

}
