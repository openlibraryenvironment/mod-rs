databaseChangeLog = {
    changeSet(author: "jskomorowski", id: "20240226-1000-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_article_title", type: "varchar(255)")
            column(name: "pr_journal_title", type: "varchar(255)")
            column(name: "pr_pages", type: "varchar(255)")
        }
    }
}
