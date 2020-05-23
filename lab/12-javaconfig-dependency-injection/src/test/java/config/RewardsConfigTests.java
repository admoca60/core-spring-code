package config;

import java.lang.reflect.Field;

import javax.sql.DataSource;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rewards.RewardNetwork;
import rewards.internal.RewardNetworkImpl;
import rewards.internal.account.AccountRepository;
import rewards.internal.account.JdbcAccountRepository;
import rewards.internal.restaurant.JdbcRestaurantRepository;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.JdbcRewardRepository;
import rewards.internal.reward.RewardRepository;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test the Spring configuration class to ensure it is creating the right beans.
 */
@SuppressWarnings("unused")
public class RewardsConfigTests {

    // Provide a mock object for testing
    private final DataSource dataSource = Mockito.mock(DataSource.class);

    // Done: Run the test
    // - Uncomment the code below between /* and */
    // - If you have implemented RewardsConfig as requested it should compile.
    // - Fix RewardsConfig if necessary.
    // - Now run the test, it should pass.


    private final RewardsConfig rewardsConfig = new RewardsConfig(this.dataSource);

    @Test
    public void getBeans() {
        final RewardNetwork rewardNetwork = this.rewardsConfig.rewardNetwork();
        assertTrue(rewardNetwork instanceof RewardNetworkImpl);

        final AccountRepository accountRepository = this.rewardsConfig.accountRepository();
        assertTrue(accountRepository instanceof JdbcAccountRepository);
        this.checkDataSource(accountRepository);

        final RestaurantRepository restaurantRepository = this.rewardsConfig.restaurantRepository();
        assertTrue(restaurantRepository instanceof JdbcRestaurantRepository);
        this.checkDataSource(restaurantRepository);

        final RewardRepository rewardsRepository = this.rewardsConfig.rewardRepository();
        assertTrue(rewardsRepository instanceof JdbcRewardRepository);
        this.checkDataSource(rewardsRepository);
    }


    /**
     * Ensure the data-source is set for the repository. Uses reflection as we do not wish to provide a
     * <tt>getDataSource()</tt> method.
     * @param repository One of our three repositories.
     */
    private void checkDataSource(final Object repository) {
        final Class<? extends Object> repositoryClass = repository.getClass();

        try {
            final Field dataSource = repositoryClass.getDeclaredField("dataSource");
            dataSource.setAccessible(true);
            assertNotNull(dataSource.get(repository));
        } catch (final Exception e) {
            final String failureMessage = "Unable to validate dataSource in " + repositoryClass.getSimpleName();
            System.out.println(failureMessage);
            e.printStackTrace();
            Fail.fail(failureMessage);
        }
    }

}
