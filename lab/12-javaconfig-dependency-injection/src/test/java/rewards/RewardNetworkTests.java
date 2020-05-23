/**
 * RewardNetworkTests.java 23 may. 2020
 *
 * Copyright 2020 INDITEX. Departamento de Sistemas
 */
package rewards;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import common.money.MonetaryAmount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="adrianmc@ext.inditex.com">Adrián Molina Calvo</a>
 *
 */
public class RewardNetworkTests {

    private RewardNetwork rewardNetwork;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        final ConfigurableApplicationContext appContext = new AnnotationConfigApplicationContext(
                TestInfrastructureConfig.class);
        this.rewardNetwork = appContext.getBean("rewardNetwork", RewardNetwork.class);

        appContext.registerShutdownHook();

    }

    @Test
    void testRewardForDining() {
        // create a new dining of 100.00 charged to credit card '1234123412341234' by merchant '123457890'
        // as test input
        final Dining dining = Dining.createDining("100.00", "1234123412341234", "1234567890");

        // call the 'rewardNetwork' to test its rewardAccountFor(Dining) method
        final RewardConfirmation confirmation = this.rewardNetwork.rewardAccountFor(dining);

        // assert the expected reward confirmation results
        assertNotNull(confirmation);
        assertNotNull(confirmation.getConfirmationNumber());

        // assert an account contribution was made
        final AccountContribution contribution = confirmation.getAccountContribution();
        assertNotNull(contribution);

        // the account number should be '123456789'
        assertEquals("123456789", contribution.getAccountNumber());

        // the total contribution amount should be 8.00 (8% of 100.00)
        assertEquals(MonetaryAmount.valueOf("8.00"), contribution.getAmount());

        // the total contribution amount should have been split into 2 distributions
        assertEquals(2, contribution.getDistributions().size());

        // each distribution should be 4.00 (as both have a 50% allocation)
        assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Annabelle").getAmount());
        assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Corgan").getAmount());
    }

}
