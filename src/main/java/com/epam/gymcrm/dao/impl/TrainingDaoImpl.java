package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Dao
public class TrainingDaoImpl implements TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Training training) {
        if (training.getTrainingId() != null) {
            entityManager.merge(training);
        } else {
            entityManager.persist(training);
        }
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));

    }

    @Override
    public List<Training> findByTraineeUsernameAndCriteria(String traineeUsername, TraineeTrainingCriteria criteria) {
        TraineeTrainingCriteria effectiveCriteria = criteria == null ? TraineeTrainingCriteria.empty() : criteria;
        StringBuilder jpql = new StringBuilder("""
                select tr
                from Training tr
                join fetch tr.trainee trainee
                join fetch trainee.user traineeUser
                join fetch tr.trainer trainer
                join fetch trainer.user trainerUser
                join fetch tr.trainingType trainingType
                where traineeUser.username = :traineeUsername
                """);

        appendDateCriteria(jpql, effectiveCriteria.fromDate(), effectiveCriteria.toDate());
        if (isNotBlank(effectiveCriteria.trainerName())) {
            jpql.append("""
                    and (
                        lower(trainerUser.firstName) like :trainerName
                        or lower(trainerUser.lastName) like :trainerName
                    )
                    """);
        }
        if (isNotBlank(effectiveCriteria.trainingType())) {
            jpql.append("and trainingType.trainingTypeName = :trainingType\n");
        }
        jpql.append("order by tr.trainingDate");

        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class)
                .setParameter("traineeUsername", traineeUsername);
        setDateCriteria(query, effectiveCriteria.fromDate(), effectiveCriteria.toDate());
        if (isNotBlank(effectiveCriteria.trainerName())) {
            query.setParameter("trainerName", toLikePattern(effectiveCriteria.trainerName()));
        }
        if (isNotBlank(effectiveCriteria.trainingType())) {
            query.setParameter("trainingType", effectiveCriteria.trainingType());
        }
        return query.getResultList();
    }

    @Override
    public List<Training> findByTrainerUsernameAndCriteria(String trainerUsername, TrainerTrainingCriteria criteria) {
        TrainerTrainingCriteria effectiveCriteria = criteria == null ? TrainerTrainingCriteria.empty() : criteria;
        StringBuilder jpql = new StringBuilder("""
                select tr
                from Training tr
                join fetch tr.trainee trainee
                join fetch trainee.user traineeUser
                join fetch tr.trainer trainer
                join fetch trainer.user trainerUser
                join fetch tr.trainingType trainingType
                where trainerUser.username = :trainerUsername
                """);

        appendDateCriteria(jpql, effectiveCriteria.fromDate(), effectiveCriteria.toDate());
        if (isNotBlank(effectiveCriteria.traineeName())) {
            jpql.append("""
                    and (
                        lower(traineeUser.firstName) like :traineeName
                        or lower(traineeUser.lastName) like :traineeName
                    )
                    """);
        }
        jpql.append("order by tr.trainingDate");

        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class)
                .setParameter("trainerUsername", trainerUsername);
        setDateCriteria(query, effectiveCriteria.fromDate(), effectiveCriteria.toDate());
        if (isNotBlank(effectiveCriteria.traineeName())) {
            query.setParameter("traineeName", toLikePattern(effectiveCriteria.traineeName()));
        }
        return query.getResultList();
    }

    private static void appendDateCriteria(StringBuilder jpql, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null) {
            jpql.append("and tr.trainingDate >= :fromDate\n");
        }
        if (toDate != null) {
            jpql.append("and tr.trainingDate <= :toDate\n");
        }
    }

    private static void setDateCriteria(TypedQuery<Training> query, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String toLikePattern(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}
