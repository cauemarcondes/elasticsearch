package org.elasticsearch.threadpool;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.settings.Validator;
import org.elasticsearch.test.ESTestCase;
import org.junit.Before;

import java.util.*;

import static org.junit.Assert.*;

public class ThreadPoolTypeSettingsValidatorTests extends ESTestCase {
    private Validator validator;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        validator = ThreadPool.THREAD_POOL_TYPE_SETTINGS_VALIDATOR;
    }

    public void testValidThreadPoolTypeSettings() {
        for (Map.Entry<String, ThreadPool.ThreadPoolType> entry : ThreadPool.THREAD_POOL_TYPES.entrySet()) {
            assertNull(validateSetting(validator, entry.getKey(), entry.getValue().getType()));
        }
    }

    public void testInvalidThreadPoolTypeSettings() {
        for (Map.Entry<String, ThreadPool.ThreadPoolType> entry : ThreadPool.THREAD_POOL_TYPES.entrySet()) {
            Set<ThreadPool.ThreadPoolType> set = new HashSet<>();
            set.addAll(Arrays.asList(ThreadPool.ThreadPoolType.values()));
            set.remove(entry.getValue());
            ThreadPool.ThreadPoolType invalidThreadPoolType = randomFrom(set.toArray(new ThreadPool.ThreadPoolType[set.size()]));
            String expectedMessage = String.format(
                    Locale.ROOT,
                    "thread pool type for [%s] can only be updated to [%s] but was [%s]",
                    entry.getKey(),
                    entry.getValue().getType(),
                    invalidThreadPoolType.getType());
            String message = validateSetting(validator, entry.getKey(), invalidThreadPoolType.getType());
            assertNotNull(message);
            assertEquals(expectedMessage, message);
        }
    }

    public void testNonThreadPoolTypeSetting() {
        String setting = ThreadPool.THREADPOOL_GROUP + randomAsciiOfLength(10) + "foo";
        String value = randomAsciiOfLength(10);
        assertNull(validator.validate(setting, value, ClusterState.PROTO));
    }

    private String validateSetting(Validator validator, String threadPoolName, String value) {
        return validator.validate(ThreadPool.THREADPOOL_GROUP + threadPoolName + ".type", value, ClusterState.PROTO);
    }
}
