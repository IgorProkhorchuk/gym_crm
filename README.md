# Gym CRM

Gym CRM is a backend application designed to manage the core operations of a fitness center. Built with the Spring Framework, it provides functionalities to manage trainees, trainers, and their training sessions.

## Features

*   **User Management:** Create and manage profiles for both Trainees and Trainers.
*   **Automatic Credentials:** Auto-generates unique usernames (e.g., `FirstName.LastName`) and random passwords for new users.
*   **Training Sessions:** Schedule and manage training sessions between trainees and trainers, including tracking the training type and duration.
*   **In-Memory Storage:** Utilizes an in-memory data store for quick development and testing, pre-populated with data from a JSON file using Spring's `BeanPostProcessor`.

## Getting Started

### Prerequisites

*   Java 17 or higher
*   Maven or Gradle

### Installation

1.  Clone the repository:
    ```bash
    git clone https://git.epam.com/ihor_prokhorchuk/gym_crm.git
    cd gym_crm
    ```
2.  Build the project:
    ```bash
    ./mvnw clean install
    ```
3.  Run the application:
    ```bash
    ./mvnw spring-boot:run
    ```

## Architecture & Design

The application follows a standard layered architecture:
*   **Services:** Contain business logic (e.g., `TraineeService`, `TrainerService`).
*   **DAOs (Data Access Objects):** Handle data persistence operations.
*   **Storage:** Custom `InMemoryStorage` initialized via `StorageInitializer` reading from `data.json`.

### Class Diagram (Entity Relations)

```mermaid
classDiagram
    class User {
        <<abstract>>
        +Long userId
        +String firstName
        +String lastName
        +String username
        +String password
        +Boolean active
    }

    class Trainee {
        +LocalDate dateOfBirth
        +String address
    }

    class Trainer {
        +String specialization
    }

    class Training {
        +Long trainingId
        +Long traineeId
        +Long trainerId
        +String trainingName
        +TrainingType trainingType
        +LocalDate trainingDate
        +Duration trainingDuration
    }

    class TrainingType {
        +Long trainingTypeId
        +String trainingTypeName
    }

    User <|-- Trainee
    User <|-- Trainer
    Trainer "1" --> "*" Training : conducts
    Trainee "1" --> "*" Training : attends
    Training "*" --> "1" TrainingType : has
```

### Component Architecture

```mermaid
graph TD
    subgraph Services
        TraineeService[Trainee Service]
        TrainerService[Trainer Service]
    end

    subgraph DAOs
        TraineeDao[Trainee DAO]
        TrainerDao[Trainer DAO]
        TrainingDao[Training DAO]
    end

    subgraph Storage Layer
        InMemoryStorage[(In-Memory Storage)]
        StorageInitializer[Storage Initializer]
    end
    
    DataJSON[data.json] -->|Reads on startup| StorageInitializer
    StorageInitializer -->|Populates| InMemoryStorage

    TraineeService --> TraineeDao
    TrainerService --> TrainerDao
    
    TraineeDao --> InMemoryStorage
    TrainerDao --> InMemoryStorage
    TrainingDao --> InMemoryStorage
```

### User Flow: Creating a New Trainee

```mermaid
sequenceDiagram
    participant Client
    participant TraineeService
    participant TraineeDao
    participant InMemoryStorage

    Client->>TraineeService: create(Trainee)
    activate TraineeService
    
    TraineeService->>TraineeDao: findAll()
    TraineeDao-->>TraineeService: List~Trainee~
    
    Note over TraineeService: Generate unique username<br/>(e.g., John.Doe1)
    TraineeService->>TraineeService: generateUsername()
    
    Note over TraineeService: Generate random 10-char password
    TraineeService->>TraineeService: generateRandomPassword()
    
    TraineeService->>TraineeDao: save(Trainee)
    activate TraineeDao
    TraineeDao->>InMemoryStorage: put(userId, Trainee)
    InMemoryStorage-->>TraineeDao: Success
    TraineeDao-->>TraineeService: Success
    deactivate TraineeDao
    
    TraineeService-->>Client: void (Success)
    deactivate TraineeService
```

## Configuration

Initial data is loaded from `src/main/resources/data.json` at application startup. The file path can be configured in `application.properties` using the `storage.file.path` property.
