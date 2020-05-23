package config;

import javax.sql.DataSource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Done-07: Perform component-scanning - Add an appropriate annotation to this class to cause
 * component scanning. - Set the base package to pick up all the classes we have annotated so far. -
 * Save all changes, Re-run the RewardNetworkTests. It should now pass.
 */
@Configuration
@ComponentScan(basePackages = { "rewards" })
public class RewardsConfig {

    DataSource dataSource;

    // @Autowired
    // public RewardsConfig(DataSource dataSource) {
    // this.dataSource = dataSource;
    // }

    // @Bean
    // public RewardNetwork rewardNetwork(){
    // return new RewardNetworkImpl(
    // accountRepository(),
    // restaurantRepository(),
    // rewardRepository());
    // }
    //
    // @Bean
    // public AccountRepository accountRepository(){
    // JdbcAccountRepository repository = new JdbcAccountRepository();
    // repository.setDataSource(dataSource);
    // return repository;
    // }
    //
    // @Bean
    // public RestaurantRepository restaurantRepository(){
    // JdbcRestaurantRepository repository = new JdbcRestaurantRepository(dataSource);
    // return repository;
    // }
    //
    // @Bean
    // public RewardRepository rewardRepository(){
    // JdbcRewardRepository repository = new JdbcRewardRepository();
    // repository.setDataSource(dataSource);
    // return repository;
    // }

    // Done-02: Remove all of the @Bean methods above.
    // - Remove the code that autowires DataSource.
    // - Run the RewardNetworkTests test. It should fail. Why? Because the bean is not created

}
