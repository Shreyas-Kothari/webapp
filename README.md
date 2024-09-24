# webapp

The repositry for CSYE6225 Cloud Computing course

## Spring Boot Project

This repository contains the backend project for the CSYE6225 Cloud Computing course.

### Project Structure

- **CloudDemo_CSYE_6225/**: Contains the source code of the application.
- **pom.xml**: Maven configuration file.

### Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher

### Building the Project

To build the Maven project, follow these steps:

1. **Clone the repository**:

    ```sh
    git clone https://github.com/spkothari0/webapp-fork/tree/main
    cd CloudDemo_CSYE_6255
    ```

2. **Build the project using Maven**:

    ```sh
    mvn clean install
    ```

3. **Run the application**:

    ```sh
    mvn spring-boot:run
    ```

### Configuration

Configuration files are located in the `src/main/resources` directory. Modify the `application.properties` file to set your own configurations.Additionally, update the environment variables mentioned in the `.env.example` file.

### Running Tests

To run the tests, use the following command:

```sh
mvn test   
```

### Deployment

Instructions for deploying the application will be provided later.

### License

This project is licensed under the MIT License.

### Contact

For any queries, please contact the repository owner at kothari.shr@northeastern.edu.
