package config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rewards.RewardNetwork;
import rewards.internal.RewardNetworkImpl;
import rewards.internal.account.AccountRepository;
import rewards.internal.account.JdbcAccountRepository;
import rewards.internal.restaurant.JdbcRestaurantRepository;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.JdbcRewardRepository;
import rewards.internal.reward.RewardRepository;

/**
 * Done: Make this class a Spring configuration class - Use an appropriate annotation.
 *
 * Done: Define four empty @Bean methods, one for the reward-network and three for the repositories.
 * - The names of the beans should be: - rewardNetwork - accountRepository - restaurantRepository -
 * rewardRepository
 *
 * Done: Inject DataSource through constructor injection - Each repository implementation has a
 * DataSource property to be set, but the DataSource is defined elsewhere
 * (TestInfrastructureConfig.java), so you will need to define a constructor for this class that
 * accepts a DataSource parameter. - As it is the only constructor, @Autowired is optional.
 *
 * Done: Implement each @Bean method to contain the code needed to instantiate its object and set
 * its dependencies - You can create beans from the following implementation classes - rewardNetwork
 * bean from rewardNetworkImpl class - accountRepository bean from JdbcAccountRepository class -
 * restaurantRepository bean from JdbcRestaurantRepository class - rewardRepository bean from
 * JdbcRewardRepository class - Note that return type of each bean method should be an interface not
 * an implementation.
 */
@Configuration
public class RewardsConfig {

    // Set this by adding a constructor.
    private final DataSource dataSource;


    /**
     * @param dataSource
     */
    public RewardsConfig(final DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    @Bean
    public AccountRepository accountRepository() {
        final JdbcAccountRepository jdbcAccountRepository = new JdbcAccountRepository(this.dataSource);
        return jdbcAccountRepository;
    }

    @Bean
    public RestaurantRepository restaurantRepository() {
        final JdbcRestaurantRepository jdbcRestaurantRepository = new JdbcRestaurantRepository(this.dataSource);
        return jdbcRestaurantRepository;
    }

    @Bean
    public RewardRepository rewardRepository() {
        final JdbcRewardRepository jdbcRewardRepository = new JdbcRewardRepository(this.dataSource);
        return jdbcRewardRepository;
    }

    @Bean
    public RewardNetwork rewardNetwork() {
        return new RewardNetworkImpl(this.accountRepository(), this.restaurantRepository(), this.rewardRepository());
    }

}
