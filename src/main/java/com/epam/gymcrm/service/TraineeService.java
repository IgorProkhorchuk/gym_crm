package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {
    private TraineeDao traineeDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    public void create(Trainee trainee) {
        trainee.setUsername(generateUsername(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(generateRandomPassword());
        traineeDao.save(trainee);
    }

    public Optional<Trainee> findById(Long id) {
        return traineeDao.findById(id);
    }

    private String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        List<Trainee> all = traineeDao.findAll();

        long count = all.stream()
                .filter(t -> t.getUsername().startsWith(baseUsername))
                .count();

        return count > 0 ? baseUsername + count : baseUsername;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i ++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
