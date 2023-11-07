package dao.imp;

import common.Constants;
import common.SqlQueries;
import dao.OrdersDAO;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.Order;
import model.errors.OrderError;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDB implements OrdersDAO {
    private DBConnectionPool db;

    @Inject
    public OrderDB(DBConnectionPool db) {
        this.db = db;
    }

    @Override
    public Either<OrderError, List<Order>> getAll() {
        Either<OrderError, List<Order>> result;
        try (Connection myConnection = db.getConnection();
             Statement statement = myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = statement.executeQuery(SqlQueries.SELECT_FROM_ORDERS);

            result=Either.right(readRS(rs).get());
            db.closeConnection(myConnection);
        } catch (SQLException e) {
            result=Either.left(new OrderError(Constants.ERROR_WHILE_RETRIEVING_ORDERS));
        }
        return result;
    }

    public Either<OrderError, Integer> delete(Order c) {
        Either<OrderError, Integer> result;
        try (Connection myConnection = db.getConnection();
             PreparedStatement preparedStatement = myConnection.prepareStatement(SqlQueries.DELETE_FROM_ORDERS_WHERE_ORDER_ID);
             PreparedStatement preparedStatementItems = myConnection.prepareStatement(SqlQueries.DELETE_FROM_ORDER_ITEMS_WHERE_ORDER_ID)) {
            preparedStatement.setInt(1, c.getId());
            preparedStatementItems.setInt(1, c.getId());
            preparedStatementItems.executeUpdate();
            preparedStatement.executeUpdate();
            result = Either.right(1);
            db.closeConnection(myConnection);
        } catch (SQLException ex) {
            result = Either.left(new OrderError(Constants.ERROR_CONNECTING_TO_DATABASE));
        }

        return result;
    }


    private Either<OrderError, List<Order>> readRS(ResultSet rs) {
        try {
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt(Constants.ORDER_ID),
                        rs.getTimestamp(Constants.ORDER_DATE).toLocalDateTime(),
                        rs.getInt(Constants.CUSTOMER_ID2),
                        rs.getInt(Constants.TABLE_ID2)
                );

                orders.add(order);

            }

            return Either.right(orders);
        } catch (SQLException e) {
            return Either.left(new OrderError(Constants.ERROR_WHILE_READING_ORDERS));
        }
    }


    public Either<OrderError, Integer> update(Order c) {
        Either<OrderError, Integer> result;
        try (Connection con = db.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(SqlQueries.UPDATE_ORDERS_SET_ORDER_DATE_CUSTOMER_ID_TABLE_ID_WHERE_ORDER_ID + "  ")) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(c.getDate()));
            preparedStatement.setInt(2, c.getCustomer_id());
            preparedStatement.setInt(3, c.getTable_id());
            preparedStatement.setInt(4, c.getId());
            int rs = preparedStatement.executeUpdate();
            if (rs == 0) {
                result = Either.left(new OrderError(Constants.ERROR_UPDATING_ORDER));
            } else {
                result = Either.right(0);
            }
            db.closeConnection(con);
        } catch (SQLException ex) {
            result = Either.left(new OrderError(Constants.ERROR_CONNECTING_TO_DATABASE));
        }
        return result;
    }

    @Override
    public Either<OrderError, Order> save(Order c) {
        Either<OrderError, Order> result = null;
        try(Connection con = db.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(SqlQueries.INSERT_INTO_ORDERS_ORDER_DATE_CUSTOMER_ID_TABLE_ID_VALUES, Statement.RETURN_GENERATED_KEYS))  {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(c.getDate()));
            preparedStatement.setInt(2, c.getCustomer_id());
            preparedStatement.setInt(3, c.getTable_id());

            int rs = preparedStatement.executeUpdate();
            if (rs == 0) {
                result = Either.left(new OrderError(Constants.ERROR_SAVING_ORDERS));
            } else {
                result = Either.right(new Order(c.getDate(), c.getCustomer_id(), c.getTable_id()));
            }
            db.closeConnection(con);
        } catch (SQLException ex) {
            result = Either.left(new OrderError(Constants.ERROR_CONNECTING_TO_DATABASE));
        }
        return result;
    }
}
