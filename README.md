# Color Stake Backend

The Color Stake Backend powers the core logic of the bidding system, allowing users to place bets on two colors. The winning color is determined by the one with the lowest total bet amount. This backend is built using Spring Boot and integrates a payment system using PayU.

## Frontend
[Frontend](https://github.com/Harshyadav00/colorstake.git)

## Getting Started


1. Clone the repository
   
   ```
   git clone https://github.com/Harshyadav00/colorStake-backend.git
   cd colorStack-backend
   ```
2. Configure the enviorment:
    
  Update `application.properties` with your database and PayU credentials:
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/bidding
    spring.datasource.username=root
    spring.datasource.password=yourpassword
    
    payu.key=your-payu-key
    payu.salt=your-payu-salt
    
    jwt.secret=your-secret-key
   ```

3. Rund the application
   ```
   mvn clean spring-boot:run
   ```
   Access the API at `http://localhost:8080`.


## License 
This Project is licensed under the MIT License.
