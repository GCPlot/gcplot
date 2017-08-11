package com.gcplot.model.account.orientdb;

import com.gcplot.Identifier;
import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.OrientDbConfigurationManager;
import com.gcplot.model.account.Account;
import com.gcplot.model.account.AccountImpl;
import com.gcplot.model.role.RestrictionType;
import com.gcplot.model.role.RoleImpl;
import com.gcplot.repository.*;
import com.gcplot.triggers.AbstractTrigger;
import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.BinaryTriggerImpl;
import com.gcplot.triggers.Trigger;
import com.gcplot.triggers.TriggerType;
import com.gcplot.triggers.binary.State;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class OrientDbRepositoryTest {

    protected OrientDbConfig config;
    protected ODatabaseDocumentTx database;

    @Before
    public void setUp() throws Exception {
        int i = new Random().nextInt();
        database = new ODatabaseDocumentTx("memory:test" + i).create();
        config = new OrientDbConfig("memory:test" + i, "admin", "admin");
    }

    @After
    public void tearDown() throws Exception {
        new ODatabaseDocumentTx(config.connectionString).open(config.user, config.password).drop();
    }

    @Test
    public void test() throws Exception {
        AccountOrientDbRepository repository = new AccountOrientDbRepository(config, new OPartitionedDatabasePoolFactory());
        repository.init();
        Assert.assertFalse(repository.account("token").isPresent());
        AccountImpl account = AccountImpl.createNew("abc", "Artem", "Dmitriev",
                "artem@reveno.org", "token", "pass", "salt", new ArrayList<>(), DateTime.now(DateTimeZone.UTC), Collections.emptySet());
        account = (AccountImpl) repository.insert(account);
        Assert.assertNotNull(account.getOId());
        Assert.assertTrue(repository.account("token").isPresent());
        Assert.assertFalse(repository.account("token").get().isConfirmed());
        Assert.assertTrue(repository.confirm("token", "salt"));
        Account account1 = repository.account("token").get();
        Assert.assertTrue(account1.isConfirmed());
        Assert.assertFalse(account1.isBlocked());

        Assert.assertEquals(repository.accounts().size(), 1);
        Assert.assertNotNull(repository.account(account1.id()).get());

        Assert.assertTrue(repository.account("abc", "pass", AccountRepository.LoginType.USERNAME).isPresent());

        repository.block("abc");
        account1 = repository.account("token").get();
        Assert.assertTrue(account1.isBlocked());

        account.setEmail("another@mail.ru");
        repository.updateInfo(account);

        account1 = repository.account("token").get();
        Assert.assertEquals(account.email(), account1.email());
        Assert.assertTrue(account1.isBlocked());

        repository.delete(account1);
        Assert.assertEquals(repository.accounts().size(), 0);
        repository.destroy();
    }

    @Test
    public void testRoles() throws Exception {
        OPartitionedDatabasePoolFactory poolFactory = new OPartitionedDatabasePoolFactory();
        AccountOrientDbRepository accRep = new AccountOrientDbRepository(config, poolFactory);
        accRep.init();
        RolesOrientDbRepository rolesRep = new RolesOrientDbRepository(config, poolFactory);
        rolesRep.init();

        RoleImpl role = new RoleImpl("test_role", Lists.newArrayList(
                new RoleImpl.RestrictionImpl(RestrictionType.TOGGLE, true, "one", 0, Collections.emptyMap()),
                new RoleImpl.RestrictionImpl(RestrictionType.QUANTITATIVE, false, "two", 5, Collections.emptyMap())));
        role = (RoleImpl) rolesRep.store(role);

        Assert.assertEquals(1, rolesRep.roles().size());

        Assert.assertEquals("test_role", role.title());
        Assert.assertEquals(2, role.restrictions().size());

        Assert.assertEquals(RestrictionType.TOGGLE, role.restrictions().get(0).type());
        Assert.assertEquals(true, role.restrictions().get(0).restricted());
        Assert.assertEquals("one", role.restrictions().get(0).action());
        Assert.assertEquals(0, role.restrictions().get(0).amount());
        Assert.assertEquals(0, role.restrictions().get(0).properties().size());

        Assert.assertEquals(RestrictionType.QUANTITATIVE, role.restrictions().get(1).type());
        Assert.assertEquals("two", role.restrictions().get(1).action());
        Assert.assertEquals(false, role.restrictions().get(1).restricted());
        Assert.assertEquals(5L, role.restrictions().get(1).amount());
        Assert.assertEquals(0, role.restrictions().get(1).properties().size());

        AccountImpl account = AccountImpl.createNew("abc", "Artem", "Dmitriev",
                "artem@reveno.org", "token", "pass", "salt", Lists.newArrayList(role), DateTime.now(DateTimeZone.UTC), Collections.emptySet());
        accRep.insert(account);
        account = (AccountImpl) accRep.account("token").get();

        Assert.assertEquals(1, account.roles().size());
        Assert.assertEquals(role, account.roles().get(0));

        role.setTitle("another_title");
        role = (RoleImpl) rolesRep.store(role);
        account = (AccountImpl) accRep.account("token").get();

        Assert.assertEquals(role, account.roles().get(0));

        // add new existing role to the existing account
        RoleImpl role2 = new RoleImpl("empty_role", Lists.newArrayList(), true);
        role2 = (RoleImpl) rolesRep.store(role2);

        accRep.attachRole(account, role2);

        account = (AccountImpl) accRep.account("token").get();

        Assert.assertEquals(2, account.roles().size());
        Assert.assertEquals(role2, account.roles().get(1));

        accRep.destroy();
        rolesRep.destroy();
    }

    @Test
    public void testFilters() throws Exception {
        FiltersOrientDbRepository repository = new FiltersOrientDbRepository(config, new OPartitionedDatabasePoolFactory());
        repository.init();
        Assert.assertEquals(0, repository.getAllFiltered("type1").size());
        repository.filter("type1", "value1");
        repository.filter("type1", "value2");
        Assert.assertEquals(2, repository.getAllFiltered("type1").size());
        Assert.assertArrayEquals(new String[] { "value1", "value2" }, repository.getAllFiltered("type1").toArray());
        repository.notFilter("type1", "value2");
        Assert.assertEquals(1, repository.getAllFiltered("type1").size());

        repository.destroy();
    }

    @Test
    public void testConfigs() throws Exception {
        OrientDbConfigurationManager cm = new OrientDbConfigurationManager(config, new OPartitionedDatabasePoolFactory());
        cm.setHostGroup("dev");
        cm.init();

        cm.putProperty(ConfigProperty.TEST1_CONFIG, "value");
        cm.putProperty(ConfigProperty.POLL_INTERVAL, 16);
        Assert.assertEquals("value", cm.readString(ConfigProperty.TEST1_CONFIG));
        Assert.assertEquals(16, cm.readInt(ConfigProperty.POLL_INTERVAL));

        cm.destroy();

        cm = new OrientDbConfigurationManager(config, new OPartitionedDatabasePoolFactory());
        cm.setHostGroup("dev");
        cm.init();
        Assert.assertEquals("value", cm.readString(ConfigProperty.TEST1_CONFIG));
        Assert.assertEquals(16, cm.readInt(ConfigProperty.POLL_INTERVAL));
        cm.destroy();
    }

    @Test
    public void testTriggers() throws Exception {
        TriggerOrientDbRepository r = new TriggerOrientDbRepository(config, new OPartitionedDatabasePoolFactory());
        r.init();
        Identifier accId = Identifier.fromStr("123");

        BinaryTriggerImpl btr = new BinaryTriggerImpl();
        btr.setState(State.ACQUIRED);
        btr.setPreviousState(State.WAIT);
        btr.setProperties(Collections.singletonMap("one", "two"));
        btr.setAccountId(accId);
        btr.setLastTimeTrigger(System.currentTimeMillis());
        btr.setType(TriggerType.IS_GCPC_WORK);
        r.saveTrigger(btr);

        List<Trigger> triggers = r.triggersFor(accId);
        Assert.assertEquals(1, triggers.size());
        BinaryTrigger fromDb = (BinaryTrigger) triggers.get(0);
        Assert.assertNotNull(fromDb.id());
        Assert.assertEquals(accId, fromDb.accountId());
        Assert.assertEquals(btr.lastTimeTrigger(), fromDb.lastTimeTrigger());
        Assert.assertEquals(btr.previousState(), fromDb.previousState());
        Assert.assertEquals(btr.state(), fromDb.state());
        Assert.assertEquals(btr.properties(), fromDb.properties());

        TestTrigger tt = new TestTrigger();
        tt.setState(true);
        tt.setPreviousState(false);
        tt.setAccountId(accId);

        r.saveTrigger(tt);

        triggers = r.triggersFor(accId);

        Assert.assertEquals(triggers.size(), 2);
        Assert.assertTrue(triggers.get(0) instanceof BinaryTrigger);
        Assert.assertTrue(triggers.get(1) instanceof TestTrigger);
        Assert.assertTrue((Boolean) triggers.get(1).state());
        Assert.assertFalse((Boolean) triggers.get(1).previousState());

        r.updateState(triggers.get(1).id(), false);
        tt = (TestTrigger) r.trigger(triggers.get(1).id());
        Assert.assertFalse(tt.state());

        r.updateState(triggers.get(1).id(), true);
        tt = (TestTrigger) r.trigger(triggers.get(1).id());
        Assert.assertTrue(tt.state());

        r.updateLastTimeTriggered(triggers.get(0).id(), btr.lastTimeTrigger() + 3);
        BinaryTriggerImpl btr1 = (BinaryTriggerImpl) r.trigger(triggers.get(0).id());
        Assert.assertEquals(btr1.lastTimeTrigger(), btr.lastTimeTrigger() + 3);

        r.putProperty(btr1.id(), "one", "two");
        btr1 = (BinaryTriggerImpl) r.trigger(btr1.id());
        Assert.assertEquals(btr1.properties().size(), 1);
        Assert.assertEquals(btr1.properties().get("one"), "two");

        r.destroy();
    }

    public static class TestTrigger extends AbstractTrigger<Boolean> {

        @Override
        public Boolean previousState() {
            return (Boolean) previousState;
        }

        @Override
        public Boolean state() {
            return (Boolean) state;
        }
    }

}
