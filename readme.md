# Camp Island

An underwater volcano formed a new small island in the Pacific Ocean last month. All the conditions on the island seems perfect and it was decided to open it up for the general public to experience the pristine uncharted territory.
The island is big enough to host a single campsite so everybody is very excited to visit. In order to regulate the number of people on the island, it was decided to come up with an online web application to manage the reservations. 

## System Description

The system have to allow the following functionality:

- The campsite will be free for all
- The campsite can be reserved for max 3 days
- The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance
- Reservations can be canceled anytime

- Provide information of the availability of the campsite for a given date range with the default being 1 month.

To make a reservation the user will provide his/her email & full name at the time of reserving the campsite along with intended arrival date and departure date. Also, these reservation can be modified or deleted.

------

## Technologies

The application was built using Java with Spring Boot to expose REST methods. For persists the data, the system uses Hibernate and JPA, with a H2 database.

## Documentation

The REST API provides different endpoints (Table 1) to satisfy the above requirements, which are described below:

| Method | URL               | Brief description                                            |
| ------ | ----------------- | ------------------------------------------------------------ |
| POST   | /reservation      | Creates a new reservation based in the required data         |
| GET    | /reservation      | Retrieve all reservations stored in the system within a date range passed as query parameters. |
| GET    | /reservation/{id} | Retrieve a reservation stored in the system associated at id parameter. |
| PATCH  | /reservation/{id} | Modify a reservation stored in the system associated at id parameter. It only allows to modify the arrival and departure dates. |
| DELETE | /reservation/{id} | Deletea reservation stored in the system associated at id parameter. |

*Table 1: summary of the REST API endpoints*

### POST

By a POST request, the user is able to create a new reservation into the system. In the system, a single reservation is represented with the same number of entities as the reservation days expressed in the request. For example, if a user wants to reserve the camp for three days until 2019-02-22 to 2019-02-25, into the system there will be three entities with one day duration each. One until 2019-02-22 to 2019-02-23, another to 2019-02-23 to 2019-02-24 and one last until 2019-02-24 to 2019-02-25.

##### URL

`POST /reservation`

##### Data Params

The data required by the camp is sent into the body requests (`name`, `email`, `arrivalDate` and `departureDate`) in JSON format. 

The dates fields, will be in format `'YYYY-MM-DD'`, and both the name and email are `'String'` fields. All the parameters are mandatory fields, so if one of them is missing the application will return an appropriate error message. 

- **Content example:** 

  `{`
      `"arrivalDate": "2019-02-19",`
      `"departureDate": "2019-02-22",`
      `"name": "Agustin Chirichigno",`
      `"email": "jp@gmail.com"`
  `}`

##### Success Response

- **HTTP Status:** 200 OK

- **Content:**

  `{`
      `"error": null,`
      `"content": "6018f3a7-054d-4d72-9bd4-e5722ebe1699"`
  `}`

##### Error Response

1. If arrival date is missing:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "You must specify an arrival date.",
         "content": null
     }`

2. If departure date is missing:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "You must specify a departure date.",
         "content": null
     }`

3. If the camp is occupied in that date range:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content: **

     `{
         "error": "The camp is already reserved for that date range.",
         "content": null
     }`

4. If arrival date is greater than departure date:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "Arrival date is greater than departure date.",
         "content": null
     }`

5. If the difference between the current date and departure date is greater than thirty day :

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.",
         "content": null
     }`

6. If the difference between the current date and arrival date is less than one day :

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.",
         "content": null
     }`

7. If the duration of the reservation is greater than three days:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "The campsite can be reserved for max 3 days.",
         "content": null
     }`

##### Example

`POST /reservation`

​`{`
    `"arrivalDate": "2019-02-19",`
    `"departureDate": "2019-02-22",`
    `"name": "Juan Perez",`
    `"email": "jp@gmail.com"`
`}`	

### GET	

Using a GET method, the system will return a list of all reservations stored in the system representation way, as mentioned before in POST method. This endpoint its for check the disponibility at specified range date in the query params.

##### URL

`GET /reservation`

##### Query Params

`arrival` = A arrival date for check the disponibility. It must be in `'YYYY-MM-DD'` format. **Mandatory parameter**.

`departure` = A arrival date for check the disponibility. It must be in `'YYYY-MM-DD'` format. If its missing, the system uses by default one month after than arrival date.

##### Success Response

- **HTTP Status:** 200 OK

- **Content:**

  `{`
      `"error": null,`
      `"content":[
          {
              "id": "6018f3a7-054d-4d72-9bd4-e5722ebe1699",
              "arrival_date": "2019-02-19",
              "departure_date": "2019-02-20",
              "name": "Juan Perez",
              "email": "jp@gmail.com"
          },
          {
              "id": "6018f3a7-054d-4d72-9bd4-e5722ebe1699",
              "arrival_date": "2019-02-20",
              "departure_date": "2019-02-21",
              "name": "Juan Perez",
              "email": "jp@gmail.com"
          },
          {
              "id": "6018f3a7-054d-4d72-9bd4-e5722ebe1699",
              "arrival_date": "2019-02-21",
              "departure_date": "2019-02-22",
              "name": "Juan Perez",
              "email": "jp@gmail.com"
          }
      ]`}`

##### Error Response

1. If arrival date is missing:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "You must specify an arrival date.",
         "content": null
     }`

2. If arrival date is greater than departure date:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "Arrival date is greater than departure date.",
         "content": null
     }`

##### Example

`GET /reservation?arrival=2019-02-19&2019-02-22`

### GET	

Using this GET endpoint, the system will return a determined reservation in the system representation way, as mentioned before in POST method.

##### URL

`GET /reservation/{id}`

##### Query Params

`id` = A identifier associated to a reservation. **Mandatory parameter**.

##### Success Response

- **HTTP Status:** 200 OK

- **Content:**

  `{`
      `"error": null,`
      `"content":[
          {
              "id": "6018f3a7-054d-4d72-9bd4-e5722ebe1699",
              "arrival_date": "2019-02-19",
              "departure_date": "2019-02-20",
              "name": "Juan Perez",
              "email": "jp@gmail.com"
          },
          {
              "id": "6018f3a7-054d-4d72-9bd4-e5722ebe1699",
              "arrival_date": "2019-02-20",
              "departure_date": "2019-02-21",
              "name": "Juan Perez",
              "email": "jp@gmail.com"
          },
          {
              "id": "6018f3a7-054d-4d72-9bd4-e5722ebe1699",
              "arrival_date": "2019-02-21",
              "departure_date": "2019-02-22",
              "name": "Juan Perez",
              "email": "jp@gmail.com"
          }
      ]`}`

##### Error Response

1. If id is missing:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "You must give a valid reservation id.",
         "content": null
     }`

2. If id does not exists into the system:

   - **HTTP Status:** 404 NOT FOUND

   - **Content:** 

     `{
         "error": "Reservation does not exist.",
         "content": null
     }`

##### Example

`GET /reservations/6018f3a7-054d-4d72-9bd4-e5722ebe1699`

### PATCH

Using a PATCH request, the user is able to modify a existing reservation. Only can modify the reservation date range (arrival date and departure date) with a new that has the same duration than the previous one.

##### URL

`PATCH /reservation/{id}`

##### Query Params

`id` = A identifier associated to a reservation. **Mandatory parameter**.

##### Data Params

The data required to modify an existing reservation is sent into the body requests (arrival date and departure date) in JSON format. 

The dates fields, will be in format `'YYYY-MM-DD'`. All the parameters are mandatory fields, so if one of them is missing the application will return an appropriate error message. 

- **Content example:** 

  `{`
      `"arrivalDate": "2019-02-24",`
      `"departureDate": "2019-02-27"`

  `}`

##### Success Response

- **HTTP Status:** 200 OK

- **Content:**

  `{`
      `"error": null,`
      `"content": "6018f3a7-054d-4d72-9bd4-e5722ebe1699"`
  `}`

##### Error Response

1. If id is missing:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "You must give a valid reservation id.",
         "content": null
     }`

2. If id does not exists into the system:

   - **HTTP Status:** 404 NOT FOUND

   - **Content:** 

     `{
         "error": "Reservation does not exist.",
         "content": null
     }`

3. If the new reservation does not has the same duration than the previous one:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content: **

     `{
         "error": "The new reservation's duration must be the same than the previous.",
         "content": null
     }

4. If the camp is occupied in that date range:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "The camp is already reserved for that date range.",
         "content": null
     }`

5. If arrival date is greater than departure date:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "Arrival date is greater than departure date.",
         "content": null
     }

6. If the difference between the current date and departure date is greater than thirty day :

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.",
         "content": null
     }`

7. If the difference between the current date and arrival date is less than one day :

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.",
         "content": null
     }`

8. If the duration of the reservation is greater than three days:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "The campsite can be reserved for max 3 days.",
         "content": null
     }`

##### Example

`PATCH /reservation/6018f3a7-054d-4d72-9bd4-e5722ebe1699`

`{`
    `"arrivalDate": "2019-02-23",`
    `"departureDate": "2019-02-26"`
`}`	

### DELETE

Using a DELETE request, the user is able to delete a existing reservation. The system will delete all reservation entities that it created, as mentioned before in POST method.

##### URL

`DELETE /reservation/{id}`

##### Query Params

`id` = A identifier associated to a reservation. **Mandatory parameter**.

##### Success Response

- **HTTP Status:** 200 OK
- **Content:** empty body.

##### Error Response

1. If id is missing:

   - **HTTP Status:** 400 BAD REQUEST

   - **Content:** 

     `{
         "error": "You must give a valid reservation id.",
         "content": null
     }`

2. If id does not exists into the system:

   - **HTTP Status:** 404 NOT FOUND

   - **Content:** 

     `{
         "error": "Reservation does not exist.",
         "content": null
     }`

3. If there is some error executing the request:

   - **HTTP Status:** 500 INTERNAL SERVER ERROR

   - **Content: **

     `{`
        ` "error": "There is an internal problem in the server.",`
        ` "content": null`
     `}`

##### Example

`DELETE /reservation/6018f3a7-054d-4d72-9bd4-e5722ebe1699`