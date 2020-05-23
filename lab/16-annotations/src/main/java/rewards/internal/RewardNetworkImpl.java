package rewards.internal;

import org.springframework.stereotype.Service;

import common.money.MonetaryAmount;
import rewards.AccountContribution;
import rewards.Dining;
import rewards.RewardConfirmation;
import rewards.RewardNetwork;
import rewards.internal.account.Account;
import rewards.internal.account.AccountRepository;
import rewards.internal.restaurant.Restaurant;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.RewardRepository;

/**
 * Rewards an Account for Dining at a Restaurant.
 *
 * The sole Reward Network implementation. This class is an application-layer service responsible
 * for coordinating with the domain-layer to carry out the process of rewarding benefits to accounts
 * for dining.
 *
 * Said in other words, this class implements the "reward account for dining" use case.
 */

/*
 * Done-03: Let this class to be found in component-scanning - Annotate this class with an
 * appropriate stereotype annotation to cause component-scanning to find and load this bean. -
 * Inject all 3 dependencies. Decide if you should use field injection or constructor injection.
 */
@Service
public class RewardNetworkImpl implements RewardNetwork {

    private final AccountRepository accountRepository;

    private final RestaurantRepository restaurantRepository;

    private final RewardRepository rewardRepository;

    /**
     * Creates a new reward network.
     * @param accountRepository the repository for loading accounts to reward
     * @param restaurantRepository the repository for loading restaurants that determine how much to
     *        reward
     * @param rewardRepository the repository for recording a record of successful reward transactions
     */

    public RewardNetworkImpl(final AccountRepository accountRepository, final RestaurantRepository restaurantRepository,
            final RewardRepository rewardRepository) {
        this.accountRepository = accountRepository;
        this.restaurantRepository = restaurantRepository;
        this.rewardRepository = rewardRepository;
    }

    @Override
    public RewardConfirmation rewardAccountFor(final Dining dining) {
        final Account account = this.accountRepository.findByCreditCard(dining.getCreditCardNumber());
        final Restaurant restaurant = this.restaurantRepository.findByMerchantNumber(dining.getMerchantNumber());
        final MonetaryAmount amount = restaurant.calculateBenefitFor(account, dining);
        final AccountContribution contribution = account.makeContribution(amount);
        this.accountRepository.updateBeneficiaries(account);
        return this.rewardRepository.confirmReward(contribution, dining);
    }

}
