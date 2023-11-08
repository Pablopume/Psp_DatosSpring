package dao.imp;

import common.Constants;
import common.SqlQueries;
import dao.CustomerDAO;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;
import model.Customer;
import model.errors.CustomerError;
import org.springframework.dao.DataAccessException;
import io.vavr.control.Either;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Log4j2
public class CustomerDB implements CustomerDAO {
    private final DBConnectionPool db;

    @Inject
    public CustomerDB(DBConnectionPool db) {
        this.db = db;
    }
//Catch DuplicatedKeyException

    public Either<CustomerError, List<Customer>> add(Customer customer) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(db.getDataSource());
        Either<CustomerError, List<Customer>> result = null;
        try {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(SqlQueries.INSERT_INTO_CREDENTIALS_ID_USER_NAME_PASSWORD_VALUES, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, customer.getFirst_name());
                    ps.setString(2, customer.getFirst_name().toLowerCase());
                    return ps;
                }, keyHolder);
                customer.setId(keyHolder.getKey().intValue());
                result = Either.right(getAll().get());
                jdbcTemplate.update(SqlQueries.INSERT_INTO_CUSTOMERS_ID_FIRST_NAME_LAST_NAME_EMAIL_PHONE_DATE_OF_BIRTH_VALUES, customer.getId(), customer.getFirst_name(), customer.getLast_name(), customer.getEmail(), customer.getPhone(), Date.valueOf(customer.getDob()));

        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
            result = Either.left(new CustomerError(0, Constants.ERROR_WHILE_RETRIEVING_ORDERS));
        }
        return result;
    }

    public Either<CustomerError, Integer> delete(Customer customer, boolean delete) {
        Either<CustomerError, Integer> result;
        if (!delete) {
            try (Connection con = db.getConnection();
                 PreparedStatement preparedStatement = con.prepareStatement(SqlQueries.DELETE_FROM_CUSTOMERS_WHERE_ID);
                 PreparedStatement preparedStatementCredentials = con.prepareStatement(SqlQueries.DELETE_FROM_CREDENTIALS_WHERE_ID)) {
                preparedStatement.setInt(1, customer.getId());
                preparedStatementCredentials.setInt(1, customer.getId());
                int rs = preparedStatement.executeUpdate();
                preparedStatementCredentials.executeUpdate();
                if (rs == 0) {
                    result = Either.left(new CustomerError(1, Constants.ERROR_DELETING_CUSTOMER));
                } else {
                    result = Either.right(0);
                }
                db.closeConnection(con);
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1451) {
                    result = Either.left(new CustomerError(2, Constants.THE_CUSTOMER_HAS_ORDERS));
                } else {
                    result = Either.left(new CustomerError(3, Constants.ERROR_CONNECTING_TO_DATABASE));
                }
            }
        } else {
            result = deleteRelationsWithCustomers(customer);
        }
        return result;
    }

    public Either<CustomerError, List<Customer>> update(Customer customer) {
        Either<CustomerError, List<Customer>> result = null;
        try (Connection myConnection = db.getConnection();
             PreparedStatement statement = myConnection.prepareStatement(SqlQueries.UPDATE_CUSTOMERS_SET_FIRST_NAME_LAST_NAME_EMAIL_PHONE_DATE_OF_BIRTH_WHERE_ID)) {
            statement.setString(1, customer.getFirst_name());
            statement.setString(2, customer.getLast_name());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getPhone());
            statement.setDate(5, Date.valueOf(customer.getDob()));
            statement.setInt(6, customer.getId());
            statement.executeUpdate();
            result = Either.right(getAll().get());
            db.closeConnection(myConnection);
        } catch (SQLException e) {
            result = Either.left(new CustomerError(0, Constants.ERROR_WHILE_RETRIEVING_ORDERS));
        }
        return result;
    }

    public Either<CustomerError, List<Customer>> get(int id) {
        Either<CustomerError, List<Customer>> result = null;
        try (Connection myConnection = db.getConnection();
             PreparedStatement statement = myConnection.prepareStatement(SqlQueries.SELECT_FROM_CUSTOMERS_WHERE_ID)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            result = Either.right(readRS(statement.getResultSet()).get());
            db.closeConnection(myConnection);
        } catch (SQLException e) {
            result = Either.left(new CustomerError(0, Constants.ERROR_WHILE_RETRIEVING_ORDERS));
        }
        return result;

    }

    @Override
    public Either<CustomerError, List<Customer>> getAll() {
        Either<CustomerError, List<Customer>> result = null;
        JdbcTemplate jtm = new JdbcTemplate(db.getDataSource());

        List<Customer> l = jtm.query("select * from customers", new MapCustomer());
        if(l.isEmpty()){
            result= Either.left(new CustomerError(0, Constants.ERROR_WHILE_RETRIEVING_ORDERS));
        }
        else{
            result= Either.right(l);
        }
        return result;
    }

    private Either<CustomerError, List<Customer>> readRS(ResultSet rs) {
        Either<CustomerError, List<Customer>> either;
        try {
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                Customer resultCustomer = new Customer(
                        rs.getInt(Constants.ID),
                        rs.getString(Constants.FIRST_NAME),
                        rs.getString(Constants.LAST_NAME),
                        rs.getString(Constants.EMAIL),
                        rs.getString(Constants.PHONE),
                        rs.getDate(Constants.DATE_OF_BIRTH).toLocalDate()
                );
                customers.add(resultCustomer);
            }
            either = Either.right(customers);

        } catch (SQLException e) {
            either = Either.left(new CustomerError(0, Constants.ERROR_WHILE_RETRIEVING_ORDERS));
        }
        return either;
    }

    private Either<CustomerError, Integer> deleteRelationsWithCustomers(Customer customer) {
        Either<CustomerError, Integer> result;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SqlQueries.DELETE_FROM_ORDER_ITEMS_WHERE_ORDER_ID_IN_SELECT_ORDER_ID_FROM_ORDERS_WHERE_CUSTOMER_ID);
             PreparedStatement preparedStatementDeleteOrders = con.prepareStatement(SqlQueries.DELETE_FROM_ORDERS_WHERE_CUSTOMER_ID);
             PreparedStatement preparedStatementCredentials = con.prepareStatement(SqlQueries.DELETE_FROM_CREDENTIALS_WHERE_ID4);
             PreparedStatement preparedStatementCustomer = con.prepareStatement(SqlQueries.DELETE_FROM_CUSTOMERS_WHERE_ID1)) {
            preparedStatement.setInt(1, customer.getId());
            preparedStatementDeleteOrders.setInt(1, customer.getId());
            preparedStatement.executeUpdate();
            preparedStatementDeleteOrders.executeUpdate();
            preparedStatementCustomer.setInt(1, customer.getId());
            preparedStatementCredentials.setInt(1, customer.getId());
            int rs = preparedStatementCustomer.executeUpdate();
            preparedStatementCredentials.executeUpdate();
            if (rs == 0) {
                result = Either.left(new CustomerError(1, Constants.ERROR_DELETING_CUSTOMER));
            } else {
                result = Either.right(0);
            }
            db.closeConnection(con);
        } catch (SQLException ex) {
            result = Either.left(new CustomerError(3, Constants.ERROR_CONNECTING_TO_DATABASE));
        }
        return result;
    }


}
