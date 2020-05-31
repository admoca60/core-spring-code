package rewards.internal.account;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import common.money.MonetaryAmount;
import common.money.Percentage;

/**
 * Loads accounts from a data source using the JDBC API.
 */

// -10 (Optional) : Inject JdbcTemplate directly to this repository class
// - Refactor the constructor to get the JdbcTemplate injected directly
// (instead of DataSource getting injected)
// - Refactor RewardsConfig accordingly
// - Refactor JdbcAccountRepositoryTests accordingly
// - Run JdbcAccountRepositoryTests and verity it passes

// -05: Refactor this repository to use JdbcTemplate.
// - Add a field of type JdbcTemplate.
// - Refactor the constructor to instantiate it.
// - Run the JdbcAccountRepositoryTests class. It should pass.
public class JdbcAccountRepository implements AccountRepository {


    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -07 (Optional): Refactor this method using a ResultSetExtractor.
    // - Create a private inner class called AccountExtractor which
    // implements ResultSetExtractor
    // - Let the extractData() method of the AccountExtractor to call
    // mapAccount() method, which is provided in this class, to do all the work.
    // - Use the JdbcTemplate to redo the SELECT below, using your new AccountExtractor
    // (If you prefer, use a Lambda expression instead of creating AccountExtractor)
    // - Run the JdbcAccountRepositoryTests class. It should pass.
    @Override
    public Account findByCreditCard(final String creditCardNumber) {
        final String sql = "select a.ID as ID, a.NUMBER as ACCOUNT_NUMBER, a.NAME as ACCOUNT_NAME, c.NUMBER as CREDIT_CARD_NUMBER, "
                +
                "	b.NAME as BENEFICIARY_NAME, b.ALLOCATION_PERCENTAGE as BENEFICIARY_ALLOCATION_PERCENTAGE, b.SAVINGS as BENEFICIARY_SAVINGS "
                +
                "from T_ACCOUNT a, T_ACCOUNT_CREDIT_CARD c " +
                "left outer join T_ACCOUNT_BENEFICIARY b " +
                "on a.ID = b.ACCOUNT_ID " +
                "where c.ACCOUNT_ID = a.ID and c.NUMBER = ?";

        return this.jdbcTemplate.query(sql, new ResultSetExtractor<Account>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Account extractData(final ResultSet rs) throws SQLException, DataAccessException {
                return JdbcAccountRepository.this.mapAccount(rs);
            }

        }, creditCardNumber);
        // Account account = null;
        // Connection conn = null;
        // PreparedStatement ps = null;
        // ResultSet rs = null;
        // try {
        // conn = this.dataSource.getConnection();
        // ps = conn.prepareStatement(sql);
        // ps.setString(1, creditCardNumber);
        // rs = ps.executeQuery();
        // account = this.mapAccount(rs);
        // } catch (final SQLException e) {
        // throw new RuntimeException("SQL exception occurred finding by credit card number", e);
        // } finally {
        // if (rs != null) {
        // try {
        // // Close to prevent database cursor exhaustion
        // rs.close();
        // } catch (final SQLException ex) {
        // }
        // }
        // if (ps != null) {
        // try {
        // // Close to prevent database cursor exhaustion
        // ps.close();
        // } catch (final SQLException ex) {
        // }
        // }
        // if (conn != null) {
        // try {
        // // Close to prevent database connection exhaustion
        // conn.close();
        // } catch (final SQLException ex) {
        // }
        // }
        // }
        // return account;
    }

    // -06: Refactor this method to use Spring's JdbcTemplate.
    // - Use your JdbcTemplate to replace the UPDATE below
    // (Note that an account has multiple beneficiaries)
    // - Rerun the JdbcAccountRepositoryTests. Verify it passes.
    @Override
    public void updateBeneficiaries(final Account account) {
        final String sql = "update T_ACCOUNT_BENEFICIARY SET SAVINGS = ? where ACCOUNT_ID = ? and NAME = ?";
        for (final Beneficiary beneficiary : account.getBeneficiaries()) {
            this.jdbcTemplate.update(sql, beneficiary.getSavings().asBigDecimal(), account.getEntityId(),
                    beneficiary.getName());

        }
        // Connection conn = null;
        // PreparedStatement ps = null;
        // try {
        // conn = this.dataSource.getConnection();
        // ps = conn.prepareStatement(sql);
        // for (final Beneficiary beneficiary : account.getBeneficiaries()) {
        // ps.setBigDecimal(1, beneficiary.getSavings().asBigDecimal());
        // ps.setLong(2, account.getEntityId());
        // ps.setString(3, beneficiary.getName());
        // ps.executeUpdate();
        // }
        // } catch (final SQLException e) {
        // throw new RuntimeException("SQL exception occurred updating beneficiary savings", e);
        // } finally {
        // if (ps != null) {
        // try {
        // // Close to prevent database cursor exhaustion
        // ps.close();
        // } catch (final SQLException ex) {
        // }
        // }
        // if (conn != null) {
        // try {
        // // Close to prevent database connection exhaustion
        // conn.close();
        // } catch (final SQLException ex) {
        // }
        // }
        // }
    }

    /**
     * Map the rows returned from the join of T_ACCOUNT and T_ACCOUNT_BENEFICIARY to an
     * fully-reconstituted Account aggregate.
     * @param rs the set of rows returned from the query
     * @return the mapped Account aggregate
     * @throws SQLException an exception occurred extracting data from the result set
     */
    private Account mapAccount(final ResultSet rs) throws SQLException {
        Account account = null;
        while (rs.next()) {
            if (account == null) {
                final String number = rs.getString("ACCOUNT_NUMBER");
                final String name = rs.getString("ACCOUNT_NAME");
                account = new Account(number, name);
                // set internal entity identifier (primary key)
                account.setEntityId(rs.getLong("ID"));
            }
            account.restoreBeneficiary(this.mapBeneficiary(rs));
        }
        if (account == null) {
            // no rows returned - throw an empty result exception
            throw new EmptyResultDataAccessException(1);
        }
        return account;
    }

    /**
     * Maps the beneficiary columns in a single row to an AllocatedBeneficiary object.
     * @param rs the result set with its cursor positioned at the current row
     * @return an allocated beneficiary
     * @throws SQLException an exception occurred extracting data from the result set
     */
    private Beneficiary mapBeneficiary(final ResultSet rs) throws SQLException {
        final String name = rs.getString("BENEFICIARY_NAME");
        final MonetaryAmount savings = MonetaryAmount.valueOf(rs.getString("BENEFICIARY_SAVINGS"));
        final Percentage allocationPercentage = Percentage.valueOf(rs.getString("BENEFICIARY_ALLOCATION_PERCENTAGE"));
        return new Beneficiary(name, allocationPercentage, savings);
    }

}
