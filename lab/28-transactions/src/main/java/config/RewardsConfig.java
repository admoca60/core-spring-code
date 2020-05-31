package config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import rewards.RewardNetwork;
import rewards.internal.RewardNetworkImpl;
import rewards.internal.account.AccountRepository;
import rewards.internal.account.JdbcAccountRepository;
import rewards.internal.restaurant.JdbcRestaurantRepository;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.JdbcRewardRepository;
import rewards.internal.reward.RewardRepository;


// -03: Add an annotation to instruct Spring to look for the
// @Transactional annotation.

@Configuration
@EnableTransactionManagement
public class RewardsConfig {

    @Autowired
    DataSource dataSource;

    @Bean
    public RewardNetwork rewardNetwork() {
        return new RewardNetworkImpl(
                this.accountRepository(),
                this.restaurantRepository(),
                this.rewardRepository());
    }

    @Bean
    public AccountRepository accountRepository() {
        final JdbcAccountRepository repository = new JdbcAccountRepository();
        repository.setDataSource(this.dataSource);
        return repository;
    }

    @Bean
    public RestaurantRepository restaurantRepository() {
        final JdbcRestaurantRepository repository = new JdbcRestaurantRepository();
        repository.setDataSource(this.dataSource);
        return repository;
    }

    @Bean
    public RewardRepository rewardRepository() {
        final JdbcRewardRepository repository = new JdbcRewardRepository();
        repository.setDataSource(this.dataSource);
        return repository;
    }

}
