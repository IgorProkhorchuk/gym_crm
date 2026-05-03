package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.Main;
import com.epam.gymcrm.dao.TrainerDao;
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

import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Main.class)
@Transactional
class TrainerDaoImplTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private TrainerDao trainerDao;

    @Test
    void saveShouldPersistTrainerAndFindByIdShouldReturnIt() {
        Trainer trainer = trainer("Alice", "Brown", "Alice.Brown");

        trainerDao.save(trainer);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainer> found = trainerDao.findById(trainer.getId());

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getUser().getFirstName()).isEqualTo("Alice"),
                () -> assertThat(found.get().getUser().getUsername()).isEqualTo("Alice.Brown"),
                () -> assertThat(found.get().getSpecialization()).isEqualTo("Fitness")
        );
    }

    @Test
    void saveShouldMergeExistingTrainer() {
        Trainer trainer = trainer("Bob", "Smith", "Bob.Smith");
        entityManager.persist(trainer);
        entityManager.flush();
        entityManager.clear();

        trainer.setSpecialization("Cardio");
        trainerDao.save(trainer);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainer> found = trainerDao.findById(trainer.getId());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Trainer::getSpecialization)
                .isEqualTo("Cardio");
    }

    @Test
    void findByIdShouldReturnEmptyOptionalWhenTrainerDoesNotExist() {
        Optional<Trainer> found = trainerDao.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteShouldRemoveTrainerById() {
        Trainer trainer = trainer("Carol", "White", "Carol.White");
        entityManager.persist(trainer);
        entityManager.flush();
        Long id = trainer.getId();

        trainerDao.delete(id);
        entityManager.flush();
        entityManager.clear();

        assertThat(trainerDao.findById(id)).isEmpty();
    }

    @Test
    void deleteShouldDoNothingWhenTrainerDoesNotExist() {
        trainerDao.delete(-1L);
        entityManager.flush();

        assertThat(trainerDao.findById(-1L)).isEmpty();
    }

    @Test
    void findAllShouldReturnStoredTrainers() {
        Trainer first = trainer("Diana", "Green", "Diana.Green");
        Trainer second = trainer("Ethan", "Black", "Ethan.Black");
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();
        entityManager.clear();

        List<Trainer> all = trainerDao.findAll();

        assertThat(all)
                .extracting(trainer -> trainer.getUser().getUsername())
                .contains("Diana.Green", "Ethan.Black");
    }
}
