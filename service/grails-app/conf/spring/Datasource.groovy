dataSource {
            dbCreate = "update"
            url = "jdbc:postgresql://folio-eks-1-palci-upgrade-1.cpvupbknx9nm.us-east-1.rds.amazonaws.com:5432/palci_prod?ApplicationName=mod-rs-hikari"
            username = "folio"
            password = "Mq7V1aardwKDfWDmnvvN9CAv"

            hikariMinimumIdle = 20
            hikariMaximumPoolSize = 30
            hikariIdleTimeout = 600000
            hikariConnectionTimeout = 30000
            hikariMaxLifetime = 1800000

        }
