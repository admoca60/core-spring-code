package rewards.internal.restaurant;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;

import common.money.Percentage;
import rewards.Dining;
import rewards.internal.account.Account;

/**
 * Loads restaurants from a data source using the JDBC API.
 */

// -09 (Optional) : Inject JdbcTemplate directly to this repository class
// - Refactor the constructor to get the JdbcTemplate injected directly
// (instead of DataSource getting injected)
// - Refactor RewardsConfig accordingly
// - Refactor JdbcRestaurantRepositoryTests accordingly
// - Run JdbcRestaurantRepositoryTests and verity it passes

// -04: Refactor the cumbersome JDBC in JdbcRestaurantRepository with JdbcTemplate.
// - Add a field of type JdbcTemplate.
// - Refactor the constructor to instantiate it.
// - Refactor findByMerchantNumber(..) to use the JdbcTemplate and a RowMapper
// called RestaurantRowMapper.
//
// Note #1: Create RestaurantRowMapper as an inner class
// Note #2: The mapRestaurant() method in this class contains logic which
// the RowMapper may wish to use
// (If you prefer, use a Lambda expression instead of creating RestaurantRowMapper class.)
//
// - Run JdbcRestaurantRepositoryTests and verity it passes

public class JdbcRestaurantRepository implements RestaurantRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRestaurantRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Restaurant findByMerchantNumber(final String merchantNumber) {
        final String sql = "select MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE, BENEFIT_AVAILABILITY_POLICY"
                + " from T_RESTAURANT where MERCHANT_NUMBER = ?";
        // final Restaurant restaurant = null;

        // try (Connection conn = this.dataSource.getConnection();
        // PreparedStatement ps = conn.prepareStatement(sql)) {
        // ps.setString(1, merchantNumber);
        // final ResultSet rs = ps.executeQuery();
        // this.advanceToNextRow(rs);
        // restaurant = this.mapRestaurant(rs);
        // } catch (final SQLException e) {
        // throw new RuntimeException("SQL exception occurred finding by merchant number", e);
        // }

        return this.jdbcTemplate.queryForObject(sql, (rs, n) -> {
            return this.mapRestaurant(rs);
        }, merchantNumber);
    }

    /**
     * Maps a row returned from a query of T_RESTAURANT to a Restaurant object.
     * @param rs the result set with its cursor positioned at the current row
     */
    private Restaurant mapRestaurant(final ResultSet rs) throws SQLException {
        // Get the row column data
        final String name = rs.getString("NAME");
        final String number = rs.getString("MERCHANT_NUMBER");
        final Percentage benefitPercentage = Percentage.valueOf(rs.getString("BENEFIT_PERCENTAGE"));

        // Map to the object
        final Restaurant restaurant = new Restaurant(number, name);
        restaurant.setBenefitPercentage(benefitPercentage);
        restaurant.setBenefitAvailabilityPolicy(this.mapBenefitAvailabilityPolicy(rs));
        return restaurant;
    }

    // /**
    // * Advances a ResultSet to the next row and throws an exception if there are no rows.
    // * @param rs the ResultSet to advance
    // * @throws EmptyResultDataAccessException if there is no next row
    // * @throws SQLException
    // */
    // private void advanceToNextRow(final ResultSet rs) throws EmptyResultDataAccessException,
    // SQLException {
    // if (!rs.next()) {
    // throw new EmptyResultDataAccessException(1);
    // }
    // }

    /**
     * Helper method that maps benefit availability policy data in the ResultSet to a fully-configured
     * {@link BenefitAvailabilityPolicy} object. The key column is 'BENEFIT_AVAILABILITY_POLICY', which
     * is a discriminator column containing a string code that identifies the type of policy. Currently
     * supported types are: 'A' for 'always available' and 'N' for 'never available'.
     *
     * <p>
     * More types could be added easily by enhancing this method. For example, 'W' for 'Weekdays only'
     * or 'M' for 'Max Rewards per Month'. Some of these types might require additional database column
     * values to be configured, for example a 'MAX_REWARDS_PER_MONTH' data column.
     * @param rs the result set used to map the policy object from database column values
     * @return the matching benefit availability policy
     * @throws IllegalArgumentException if the mapping could not be performed
     */
    private BenefitAvailabilityPolicy mapBenefitAvailabilityPolicy(final ResultSet rs) throws SQLException {
        final String policyCode = rs.getString("BENEFIT_AVAILABILITY_POLICY");
        if ("A".equals(policyCode)) {
            return AlwaysAvailable.INSTANCE;
        } else if ("N".equals(policyCode)) {
            return NeverAvailable.INSTANCE;
        } else {
            throw new IllegalArgumentException("Not a supported policy code " + policyCode);
        }
    }

    /**
     * Returns true indicating benefit is always available.
     */
    static class AlwaysAvailable implements BenefitAvailabilityPolicy {

        static final BenefitAvailabilityPolicy INSTANCE = new AlwaysAvailable();

        @Override
        public boolean isBenefitAvailableFor(final Account account, final Dining dining) {
            return true;
        }

        @Override
        public String toString() {
            return "alwaysAvailable";
        }

    }

    /**
     * Returns false indicating benefit is never available.
     */
    static class NeverAvailable implements BenefitAvailabilityPolicy {

        static final BenefitAvailabilityPolicy INSTANCE = new NeverAvailable();

        @Override
        public boolean isBenefitAvailableFor(final Account account, final Dining dining) {
            return false;
        }

        @Override
        public String toString() {
            return "neverAvailable";
        }

    }

}
