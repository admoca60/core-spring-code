package rewards.internal.reward;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import common.datetime.SimpleDate;
import common.money.MonetaryAmount;
import common.money.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rewards.AccountContribution;
import rewards.Dining;
import rewards.RewardConfirmation;
import rewards.internal.account.Account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests the JDBC reward repository with a test data source to verify data access and
 * relational-to-object mapping behavior works as expected.
 */
public class JdbcRewardRepositoryTests {

    private JdbcRewardRepository repository;

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() throws Exception {
        this.dataSource = this.createTestDataSource();
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.repository = new JdbcRewardRepository(this.jdbcTemplate);

    }

    @Test
    public void testCreateReward() throws SQLException {
        final Dining dining = Dining.createDining("100.00", "1234123412341234", "0123456789");

        final Account account = new Account("1", "Keith and Keri Donald");
        account.setEntityId(0L);
        account.addBeneficiary("Annabelle", Percentage.valueOf("50%"));
        account.addBeneficiary("Corgan", Percentage.valueOf("50%"));

        final AccountContribution contribution = account.makeContribution(MonetaryAmount.valueOf("8.00"));
        final RewardConfirmation confirmation = this.repository.confirmReward(contribution, dining);
        assertNotNull(confirmation, "confirmation should not be null");
        assertNotNull(confirmation.getConfirmationNumber(), "confirmation number should not be null");
        assertEquals(contribution, confirmation.getAccountContribution(), "wrong contribution object");
        this.verifyRewardInserted(confirmation, dining);
    }

    private void verifyRewardInserted(final RewardConfirmation confirmation, final Dining dining) throws SQLException {
        assertEquals(1, this.getRewardCount());

        // -02: Use the JdbcTemplate to query for a map of all values in the T_REWARD table based on the
        // confirmationNumber. After making the changes, execute the test class to verify its
        // successful execution. (If you are using Gradle, comment out the test exclude in
        // the build.gradle file.)
        //
        // SQL: SELECT * FROM T_REWARD WHERE CONFIRMATION_NUMBER = ?
        final Map<String, Object> values = this.jdbcTemplate
            .queryForMap("SELECT * FROM T_REWARD WHERE CONFIRMATION_NUMBER = ?", confirmation.getConfirmationNumber());
        this.verifyInsertedValues(confirmation, dining, values);
    }

    private void verifyInsertedValues(final RewardConfirmation confirmation, final Dining dining,
            final Map<String, Object> values) {
        assertEquals(confirmation.getAccountContribution().getAmount(), new MonetaryAmount((BigDecimal) values
            .get("REWARD_AMOUNT")));
        assertEquals(SimpleDate.today().asDate(), values.get("REWARD_DATE"));
        assertEquals(confirmation.getAccountContribution().getAccountNumber(), values.get("ACCOUNT_NUMBER"));
        assertEquals(dining.getAmount(), new MonetaryAmount((BigDecimal) values.get("DINING_AMOUNT")));
        assertEquals(dining.getMerchantNumber(), values.get("DINING_MERCHANT_NUMBER"));
        assertEquals(SimpleDate.today().asDate(), values.get("DINING_DATE"));
    }

    private int getRewardCount() throws SQLException {
        return this.jdbcTemplate.queryForObject("SELECT count(*) FROM T_REWARD", Integer.class);
        // -01: Use the JdbcTemplate to query for the number of rows in the T_REWARD table
        // SQL: SELECT count(*) FROM T_REWARD
        // return -1;
    }

    private DataSource createTestDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setName("rewards")
            .addScript("/rewards/testdb/schema.sql")
            .addScript("/rewards/testdb/data.sql")
            .build();
    }

}
