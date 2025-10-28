package base;

import dto.CategoryRow;

import java.util.Map;

import static helper.MappingHelper.toLong;

public class CategoryRepository {

    private final MainBase base;

    public CategoryRepository(MainBase base) {
        this.base = base;
    }

    public CategoryRow getCategoryById(int id) {
        String sql = "select term_id, name, slug from wp_terms where term_id = ?";
        Map<String, Object> row = base.row(sql, id);
        if (row.isEmpty()) return null;

        return new CategoryRow(
                toLong(row.get("term_id")),
                (String) row.get("name"),
                (String) row.get("slug")
        );
    }

    public void deleteCategoryById(int id) {
        String sql = "delete from wp_terms where term_id = ?";
        base.update(sql, id);
    }

    public void updateCategoryNameById(int id, String name) {
        String sql = "update wp_terms set name = ? where term_id = ?";
        base.update(sql, name, id);
    }
}
