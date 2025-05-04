package com.benchpress200.springsecuritytutorial.repository;

import com.benchpress200.springsecuritytutorial.domain.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private long currentId = 1;
    private final Map<Long, User> repository = new HashMap<>();

    public void save(final User user) {
        user.setId(currentId);
        repository.put(currentId++, user);
    }

    public User findById(final long id) {
        return repository.get(id);
    }

    public User findByName(final String name) {
        for(User user : repository.values()) {
            if(user.getName().equals(name)) {
                return user;
            }
        }

        return null;
    }

    public void update(final User user) {
        repository.put(user.getId(), user);
    }

    public void delete(final long id) {
        repository.remove(id);
    }
}
