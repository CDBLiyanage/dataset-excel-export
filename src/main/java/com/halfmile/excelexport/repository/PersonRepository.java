package com.halfmile.excelexport.repository;

import com.halfmile.excelexport.model.Person;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PersonRepository {

    private final JdbcTemplate jdbc;

    public PersonRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void truncate() {
        jdbc.execute("TRUNCATE TABLE dataset");
    }

    public void batchInsert(List<Person> rows) {
        jdbc.batchUpdate(
                "INSERT INTO dataset (id, name, email, age, country) VALUES (?, ?, ?, ?, ?)",
                rows,
                rows.size(),
                (ps, p) -> {
                    ps.setLong(1, p.id());
                    ps.setString(2, p.name());
                    ps.setString(3, p.email());
                    ps.setObject(4, p.age());
                    ps.setString(5, p.country());
                });
    }

    public long count() {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM dataset", Long.class);
        return n == null ? 0 : n;
    }

    public List<Person> findBatchAfter(long lastId, int limit) {
        return jdbc.query(
                "SELECT id, name, email, age, country FROM dataset WHERE id > ? ORDER BY id LIMIT ?",
                (rs, rowNum) -> {
                    int age = rs.getInt("age");
                    Integer ageValue = rs.wasNull() ? null : age;
                    return new Person(rs.getLong("id"), rs.getString("name"),
                            rs.getString("email"), ageValue, rs.getString("country"));
                },
                lastId, limit);
    }
}