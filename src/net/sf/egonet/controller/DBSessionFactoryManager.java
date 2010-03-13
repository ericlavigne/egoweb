/*
 * DBSessionFactoryManager.java
 *
 * Created on March 4, 2010, 10:00 AM
 */

package net.sf.egonet.controller;

import java.io.File;
import java.util.HashMap;
import net.sf.egonet.persistence.DB;
import net.sf.egonet.web.Main;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * This class is used by Egonet to support accessing an arbitrary number of
 * studies during a single execution of the Egonet server.  Given that there is
 * a notion of which study among multiple studies is the "active" one at any
 * given time, this class determines which resources are needed for that study
 * and registers them with the Egonet application.
 *
 * @author Matt Futterman
 */
class DBSessionFactoryManager
{
    private static DBSessionFactoryManager        sessionFactoryManager;
    private static HashMap<String,SessionFactory> sessionFactoryMap;

    private DBSessionFactoryManager()
    {
        sessionFactoryMap = new HashMap<String,SessionFactory>();
    }

    /**
     * This method returns the singleton instance of DBSessionFactoryManager.
     * The instance will be created the first time this method is called.
     */
    static DBSessionFactoryManager getInstance()
    {
        if (sessionFactoryManager == null)
        {
            sessionFactoryManager = new DBSessionFactoryManager();
        }
        return sessionFactoryManager;
    }

    /**
     * This method determines which database session factory to use for the
     * Egonet study named 'studyName'.  Each database session factory instance
     * is stored in a table.  If the required instance is found in the table it
     * is used, otherwise an instance is created and stored in the table.  This
     * guarantees that a database session factory is created only once per study.
     * When a database session factory is created it is configured using the
     * configuration file found in 'configFilePath'.  Finally, the retrieved or
     * created factory instance is registered as the active instance by calling
     * method Main.setDBSessionFactory().
     *
     * Throws an Exception if the configuration file pointed to by
     * 'configFilePath' could not be found.
     * Throws an Exception if the database session factory could not be
     * configured and built.
     * 
     * @param studyName name of Egonet study needing a database session factory
     * @param configFilePath path for database session factory configuration file
     * @throws Exception
     */
    static void useSessionFactory( String studyName, String configFilePath ) throws Exception
    {
        SessionFactory sessionFactory = sessionFactoryMap.get( studyName );
        if (sessionFactory == null)
        {
            File configFile = new File( configFilePath );
            if (!configFile.exists())
            {
                throw new Exception( "Hibernate configuration file " +
                                     configFile.toString() + " not found." );
            }
            try
            {
                Configuration config = new Configuration();
                sessionFactory = config.configure( configFile ).buildSessionFactory();
                sessionFactoryMap.put( studyName, sessionFactory );
                Main.setDBSessionFactory( sessionFactory );
                DB.migrate();
            }
            catch ( Throwable e )
            {
                throw new Exception( "Unable to create database session factory using configuration file.", e );
            }
        }
        else
        {
            Main.setDBSessionFactory( sessionFactory );
        }
    }
}
