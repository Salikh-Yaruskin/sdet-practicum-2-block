package base;

import dto.PostCategoryMemberRow;

import java.util.Map;

import static helper.MappingHelper.toLong;

public class PostCategoryMemberRepository {

    private MainBase base;

    public PostCategoryMemberRepository(MainBase base) {
        this.base = base;
    }

    public PostCategoryMemberRow getMember(int categoryId) {
        String sql = "select object_id, term_taxonomy_id from wp_term_relationships where term_taxonomy_id = ?";
        Map<String, Object> row = base.row(sql, categoryId);
        if (row.isEmpty()) return null;

        return new PostCategoryMemberRow(
                toLong(row.get("object_id")),
                toLong(row.get("term_taxonomy_id"))
        );
    }
}
