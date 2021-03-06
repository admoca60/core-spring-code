package rewards.internal.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;

import common.money.MonetaryAmount;
import common.money.Percentage;

/**
 * Loads accounts from a data source using the JDBC API.
 */
public class JdbcAccountRepository implements AccountRepository {

    private DataSource dataSource;


    /**
     * @param dataSource
     */
    public JdbcAccountRepository(final DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    /**
     * Sets the data source this repository will use to load accounts.
     * @param dataSource the data source
     */
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

        Account account = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = this.dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, creditCardNumber);
            rs = ps.executeQuery();
            account = this.mapAccount(rs);
        } catch (final SQLException e) {
            throw new RuntimeException("SQL exception occurred finding by credit card number", e);
        } finally {
            if (rs != null) {
                try {
                    // Close to prevent database cursor exhaustion
                    rs.close();
                } catch (final SQLException ex) {
                }
            }
            if (ps != null) {
                try {
                    // Close to prevent database cursor exhaustion
                    ps.close();
                } catch (final SQLException ex) {
                }
            }
            if (conn != null) {
                try {
                    // Close to prevent database connection exhaustion
                    conn.close();
                } catch (final SQLException ex) {
                }
            }
        }
        return account;
    }

    @Override
    public void updateBeneficiaries(final Account account) {
        final String sql = "update T_ACCOUNT_BENEFICIARY SET SAVINGS = ? where ACCOUNT_ID = ? and NAME = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = this.dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            for (final Beneficiary beneficiary : account.getBeneficiaries()) {
                ps.setBigDecimal(1, beneficiary.getSavings().asBigDecimal());
                ps.setLong(2, account.getEntityId());
                ps.setString(3, beneficiary.getName());
                ps.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new RuntimeException("SQL exception occurred updating beneficiary savings", e);
        } finally {
            if (ps != null) {
                try {
                    // Close to prevent database cursor exhaustion
                    ps.close();
                } catch (final SQLException ex) {
                }
            }
            if (conn != null) {
                try {
                    // Close to prevent database connection exhaustion
                    conn.close();
                } catch (final SQLException ex) {
                }
            }
        }
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
            final Beneficiary b = this.mapBeneficiary(rs);
            if (b != null) {
                account.restoreBeneficiary(b);
            }
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
        if (name == null) {
            // apparently no beneficiary for this
            return null;
        }
        final MonetaryAmount savings = MonetaryAmount.valueOf(rs.getString("BENEFICIARY_SAVINGS"));
        final Percentage allocationPercentage = Percentage.valueOf(rs.getString("BENEFICIARY_ALLOCATION_PERCENTAGE"));
        return new Beneficiary(name, allocationPercentage, savings);
    }

}
