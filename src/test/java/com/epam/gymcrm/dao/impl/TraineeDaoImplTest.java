package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.Main;
import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Main.class)
@Transactional
class TraineeDaoImplTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private TraineeDao traineeDao;

    @Test
    void saveShouldPersistTraineeAndFindByIdShouldReturnIt() {
        Trainee trainee = trainee("Oleg", "Petrenko", "Oleg.Petrenko");

        traineeDao.save(trainee);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainee> found = traineeDao.findById(trainee.getId());

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getUser().getFirstName()).isEqualTo("Oleg"),
                () -> assertThat(found.get().getUser().getUsername()).isEqualTo("Oleg.Petrenko"),
                () -> assertThat(found.get().getAddress()).isEqualTo("Main Street, 123")
        );
    }

    @Test
    void saveShouldMergeExistingTrainee() {
        Trainee trainee = trainee("Ivan", "Franko", "Ivan.Franko");
        entityManager.persist(trainee);
        entityManager.flush();
        entityManager.clear();

        trainee.setAddress("Updated Street, 55");
        traineeDao.save(trainee);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainee> found = traineeDao.findById(trainee.getId());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Trainee::getAddress)
                .isEqualTo("Updated Street, 55");
    }

    @Test
    void findByIdShouldReturnEmptyOptionalWhenTraineeDoesNotExist() {
        Optional<Trainee> found = traineeDao.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByUsernameShouldReturnTraineeWhenUsernameExists() {
        Trainee trainee = trainee("Maria", "Shevchenko", "Maria.Shevchenko");
        entityManager.persist(trainee);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainee> found = traineeDao.findByUsername("Maria.Shevchenko");

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getUser().getUsername()).isEqualTo("Maria.Shevchenko"),
                () -> assertThat(found.get().getUser().getFirstName()).isEqualTo("Maria")
        );
    }

    @Test
    void findByUsernameShouldReturnEmptyOptionalWhenUsernameDoesNotExist() {
        Optional<Trainee> found = traineeDao.findByUsername("Unknown.Trainee");

        assertThat(found).isEmpty();
    }

    @Test
    void saveShouldPersistAssignedTrainersRelation() {
        Trainer trainer = trainer("Assigned", "Trainer", "Assigned.Trainer");
        entityManager.persist(trainer);

        Trainee trainee = trainee("Linked", "Trainee", "Linked.Trainee");
        trainee.getTrainers().add(trainer);

        traineeDao.save(trainee);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainee> found = traineeDao.findById(trainee.getId());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Trainee::getTrainers)
                .satisfies(trainers -> assertThat(trainers)
                        .extracting(assignedTrainer -> assignedTrainer.getUser().getUsername())
                        .containsExactly("Assigned.Trainer"));
    }

    @Test
    void deleteShouldRemoveTraineeById() {
        Trainee trainee = trainee("Lesya", "Ukrainka", "Lesya.Ukrainka");
        entityManager.persist(trainee);
        entityManager.flush();
        Long id = trainee.getId();

        traineeDao.delete(id);
        entityManager.flush();
        entityManager.clear();

        assertThat(traineeDao.findById(id)).isEmpty();
    }

    @Test
    void deleteShouldDoNothingWhenTraineeDoesNotExist() {
        traineeDao.delete(-1L);
        entityManager.flush();

        assertThat(traineeDao.findById(-1L)).isEmpty();
    }

    @Test
    void findAllShouldReturnStoredTrainees() {
        Trainee first = trainee("Anna", "Taylor", "Anna.Taylor");
        Trainee second = trainee("Brian", "Miller", "Brian.Miller");
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();
        entityManager.clear();

        List<Trainee> all = traineeDao.findAll();

        assertThat(all)
                .extracting(trainee -> trainee.getUser().getUsername())
                .contains("Anna.Taylor", "Brian.Miller");
    }
}
