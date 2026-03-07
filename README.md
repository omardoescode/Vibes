# Requirements

You must have all of these tools accessible through the command line
1. `docker`
2. `npm`
3. `maven`
4. `Just`: https://github.com/casey/just

# Running the project

```
-- Use this command to list all commands runnable
just --list
```

Before you run anything, make sure that you set up `.env`. You can just copy `.env.example`
```
cp .env.example .env
```


To run everything, run
```
just dev
```

Useful commands
```
just backend_install -- use maven to install the packages
just frontned_install -- use npm to install the node modules
just db_logs -- Look at the database logs
```


