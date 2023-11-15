package dao.imp.maps;

import model.MenuItem;
import model.OrderItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MapOrderItem implements RowMapper<OrderItem> {
    @Override
    public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new OrderItem(
                rs.getInt("order_item_id"),
                rs.getInt("order_id"),
                new MenuItem(rs.getInt("menu_item_id"),rs.getString("name"),rs.getString("description"),rs.getDouble("price")),
                rs.getInt("quantity"));
    }
}
