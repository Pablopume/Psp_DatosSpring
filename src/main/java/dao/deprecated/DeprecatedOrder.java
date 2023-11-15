package dao.deprecated;

import dao.imp.DBConnectionPool;
import io.vavr.control.Either;
import jakarta.inject.Inject;
import model.Order;
import model.errors.OrderError;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeprecatedOrder {
    private final DBConnectionPool db;

    @Inject
    public DeprecatedOrder(DBConnectionPool db) {
        this.db = db;
    }


    public Either<OrderError, List<Order>> getAll() {
        try (Connection myConnection = db.getConnection();
             Statement statement = myConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = statement.executeQuery("select * from orders");
            return Either.right(readRS(rs).get());
        } catch (SQLException e) {
            return Either.left(new OrderError("Error while reading orders"));
        }
    }


    private Either<OrderError, List<Order>> readRS(ResultSet rs) {
        try {
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getInt("customer_id"),
                        rs.getInt("table_id")
                );

                orders.add(order);

            }
            return Either.right(orders);
        } catch (SQLException e) {
            return Either.left(new OrderError("Error while reading orders"));
        }

    }
}
