package ibf2021.assessment.openlibrarysearch.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import static ibf2021.assessment.openlibrarysearch.Constants.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class BookRepository {
    @Autowired
    @Qualifier(BEAN_REDIS_OPENLIBRARY)
    RedisTemplate<String, String> template;

    public void save(String key, String jsonString) {
        template.opsForValue().set(normalise(key), jsonString, 10, TimeUnit.MINUTES);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(template.opsForValue().get(normalise(key)));
    }

    private String normalise(String key) {
        return key.trim().toLowerCase();
    }
    
}
