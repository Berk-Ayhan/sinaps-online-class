### Database Container 

```
docker run -p 3307:3306 --name mysqlcontainer -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=spring_jwt -d mysql:8
```

### Backend App Container
```
docker run -p 8090:8080 --name springcontainer --net mysqlnetwork -e MYSQL_HOST=mysqlcontainer -e MYSQL_PORT=3306 -e MYSQL_DB_NAME=spring_jwt -e MYSQL_USER=root -e MYSQL_PASSWORD=root onlineclass-app
```


* Important Note: Dont forget to change database, otherwise it wont find tables.

```
USE spring_jwt
```