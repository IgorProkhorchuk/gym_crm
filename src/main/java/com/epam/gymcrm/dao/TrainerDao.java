package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.storage.InMemoryStorage;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for {@link Trainer} records keyed by {@link Trainer#getUserId()}.
 */
public interface TrainerDao {

    /**
     * Provides the storage instance used by this DAO.
     *
     * @param storage storage containing trainer records
     */
    void setStorage(InMemoryStorage storage);

    /**
     * Stores the trainer under its user id, replacing any record with the same id.
     *
     * @param trainer trainer to insert or replace
     */
    void save(Trainer trainer);

    /**
     * Finds a trainer by user id.
     *
     * @param id user id to look up
     * @return trainer with the given id, or {@link Optional#empty()} when absent
     */
    Optional<Trainer> findById(Long id);

    /**
     * Removes a trainer by user id.
     *
     * @param id user id to remove
     */
    void delete(Long id);

    /**
     * Returns a snapshot of all stored trainers.
     *
     * @return all trainers present when the method is called
     */
    List<Trainer> findAll();
}
