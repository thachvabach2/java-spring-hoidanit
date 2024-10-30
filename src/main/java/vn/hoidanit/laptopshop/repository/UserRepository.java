package vn.hoidanit.laptopshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // insert into User value ()
    // update User set ....
    User save(User eric);

    List<User> findOneByEmail(String email);

    List<User> findAll();

    User findById(long id);
}
