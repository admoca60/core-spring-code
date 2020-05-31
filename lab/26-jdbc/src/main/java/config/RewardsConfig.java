package config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import rewards.RewardNetwork;
import rewards.internal.RewardNetworkImpl;
import rewards.internal.account.AccountRepository;
import rewards.internal.account.JdbcAccountRepository;
import rewards.internal.restaurant.JdbcRestaurantRepository;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.JdbcRewardRepository;
import rewards.internal.reward.RewardRepository;

@Configuration
public class RewardsConfig {

    @Autowired
    DataSource dataSource;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(this.dataSource);
    }

    @Bean
    public RewardNetwork rewardNetwork() {
        return new RewardNetworkImpl(
                this.accountRepository(),
                this.restaurantRepository(),
                this.rewardRepository());
    }

    @Bean
    public AccountRepository accountRepository() {
        final JdbcAccountRepository repository = new JdbcAccountRepository(this.jdbcTemplate());
        return repository;
    }

    @Bean
    public RestaurantRepository restaurantRepository() {
        final JdbcRestaurantRepository repository = new JdbcRestaurantRepository(this.jdbcTemplate());
        return repository;
    }

    @Bean
    public RewardRepository rewardRepository() {
        final JdbcRewardRepository repository = new JdbcRewardRepository(this.jdbcTemplate());
        return repository;
    }

}
