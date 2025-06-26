# Document about database

## Environment Setup

For simplicity, a local H2 in-memory is used in the application.   
The related configurations are defined in the [application.yaml](../src/main/resources/application.yaml) file
- spring.datasource
- spring.h2

# Database Schema

There are 4 tables used in the code and they are defined in the [schema file](../src/main/resources/schema.sql) which will be executed every time the application starts.
- user_profiles: stores the user information
- role: definitions of available roles
- user_roles: roles that each user associates with
- exchange_rates: exchange rates data which will be updated periodically by a schedule job deifned in the code.

Initial data are defined in [data.sql](../src/main/resources/data.sql) which will be inserted into the database every time the server starts.