package helper;

import base.MainBase;

import static java.util.Objects.nonNull;

public class TestDataHelper {

    private final MainBase base;

    public Long createdTermId = null;
    public Long createdTermTaxonomyId = null;
    public Long createdPostPublishedId = null;
    public Long createdPostDraftId = null;
    public Long createdCommentId = null;

    public TestDataHelper(MainBase base) {
        this.base = base;
    }

    public Long createCategory(String name, String slug) {
        String uniqueSlug = slug + "-" + System.currentTimeMillis();
        String insertTerm = "insert into wp_terms(name, slug) values (?, ?)";
        this.createdTermId = base.insertAndGetId(insertTerm, name, uniqueSlug );

        String insertTax = "insert into wp_term_taxonomy (term_id, taxonomy, description, parent, count) values " +
                "(?, 'category', 'test_d2', 0, 0)";
        this.createdTermTaxonomyId = base.insertAndGetId(insertTax, this.createdTermId);

        return this.createdTermId;
    }

    public Long createPost(Long authorId, String title, String content, String status) {
        String insertPost = "insert into wp_posts " +
                "(post_author, post_date, post_date_gmt, post_content, post_title, post_excerpt, " +
                "post_status, comment_status, post_name, to_ping, pinged, post_modified, post_modified_gmt, " +
                "post_content_filtered, post_type) " +
                "values (?, now(), now(), ?, ?, '', ?, 'open', ?, '', '', now(), now(), '', 'post')";
        return base.insertAndGetId(insertPost,
                authorId, content, title, status, title.toLowerCase().replaceAll("\\s+", "-"));
    }

    public Long createComment(Long postId, String authorName, String authorEmail, String content, String approved) {
        String insertComment = "insert into wp_comments " +
                "(comment_post_ID, comment_author, comment_author_email, comment_date, comment_date_gmt, " +
                "comment_content, comment_approved) values (?, ?, ?, now(), now(), ?, ?)";
        long commentId = base.insertAndGetId(insertComment, postId, authorName, authorEmail, content, approved);
        this.createdCommentId = commentId;
        return commentId;
    }

    public void linkPostToCategory(Long postId, Long termTaxonomyId) {
        String insertRel = "insert into wp_term_relationships (object_id, term_taxonomy_id, term_order) values (?, ?, 0)";
        base.insertAndGetId(insertRel, postId, termTaxonomyId);

        String updateCount = "update wp_term_taxonomy set count = count + 1 where term_taxonomy_id = ?";
        base.update(updateCount, termTaxonomyId);
    }

    public void cleanup() {
        try {
            if (nonNull(createdPostPublishedId)) {
                if (nonNull(createdTermTaxonomyId)) {
                    base.update("delete from wp_term_relationships where object_id = ? and term_taxonomy_id = ?",
                            createdPostPublishedId, createdTermTaxonomyId);
                    base.update("update wp_term_taxonomy set count = GREATEST(count - 1, 0) " +
                            "where term_taxonomy_id = ?", createdTermTaxonomyId);
                }
            }
            if (nonNull(createdCommentId)) {
                base.update("delete from wp_comments where comment_ID = ?", createdCommentId);
            }
            if (nonNull(createdPostPublishedId)) {
                base.update("delete from wp_posts where ID = ?", createdPostPublishedId);
            }
            if (nonNull(createdPostDraftId)) {
                base.update("delete from wp_posts where ID = ?", createdPostDraftId);
            }
            if (nonNull(createdTermTaxonomyId)) {
                base.update("delete from wp_term_taxonomy where term_taxonomy_id = ?", createdTermTaxonomyId);
            }
            if (nonNull(createdTermId)) {
                base.update("delete from wp_terms where term_id = ?", createdTermId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
