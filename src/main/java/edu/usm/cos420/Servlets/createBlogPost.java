package edu.usm.cos420.Servlets;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Properties;

import edu.usm.cos420.domain.*;

@SuppressWarnings("serial")
@WebServlet(name = "createBlogPost", value="/create")
public class createBlogPost extends HttpServlet{

	final String cloudDBUrl = "jdbc:postgresql:///%s";
	
	final String createDbQuery =  "CREATE TABLE IF NOT EXISTS posts ( id SERIAL PRIMARY KEY, "
			+ "title VARCHAR(255), author VARCHAR(255), description VARCHAR(255))";
	final String insertPost = "INSERT INTO posts "
			+ "(title, author, description) "
			+ "VALUES (?, ?, ?)";

	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		BlogPost post = new BlogPost();
		post.setTitle(req.getParameter("title"));
		post.setAuthor(req.getParameter("author"));
		post.setDescription(req.getParameter("description"));

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Get DB information
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream("database.properties"));

		
		String dbUrl = String.format(cloudDBUrl,
				properties.getProperty("sql.dbName"));

	    HikariConfig config = new HikariConfig();

	    config.setJdbcUrl(dbUrl);
	    config.setUsername(properties.getProperty("sql.userName"));
		config.setPassword(properties.getProperty("sql.password"));
		config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
    	config.addDataSourceProperty("cloudSqlInstance", properties.getProperty("sql.instanceName"));


	    config.setMaximumPoolSize(5);
	    // minimumIdle is the minimum number of idle connections Hikari maintains in the pool.
	    // Additional connections will be established to meet this value unless the pool is full.
	    config.setMinimumIdle(5);
	    // [END cloud_sql_postgres_servlet_limit]

	    // [START cloud_sql_postgres_servlet_timeout]
	    // setConnectionTimeout is the maximum number of milliseconds to wait for a connection checkout.
	    // Any attempt to retrieve a connection from this pool that exceeds the set limit will throw an
	    // SQLException.
	    config.setConnectionTimeout(10000); // 10 seconds
	    // idleTimeout is the maximum amount of time a connection can sit in the pool. Connections that
	    // sit idle for this many milliseconds are retried if minimumIdle is exceeded.
	    config.setIdleTimeout(600000); // 10 minutes
	    // [END cloud_sql_postgres_servlet_timeout]

	    config.addDataSourceProperty("ipTypes", "PUBLIC,PRIVATE");
	    
	    // [START cloud_sql_postgres_servlet_backoff]
	    // Hikari automatically delays between failed connection attempts, eventually reaching a
	    // maximum delay of `connectionTimeout / 2` between attempts.
	    // [END cloud_sql_postgres_servlet_backoff]

	    // [START cloud_sql_postgres_servlet_lifetime]
	    // maxLifetime is the maximum possible lifetime of a connection in the pool. Connections that
	    // live longer than this many milliseconds will be closed and reestablished between uses. This
	    // value should be several minutes shorter than the database's timeout value to avoid unexpected
	    // terminations.
	    config.setMaxLifetime(1800000); // 30 minutes
	    // [END cloud_sql_postgres_servlet_lifetime]
	    
	    HikariDataSource ds = new HikariDataSource(config);
		

		PrintWriter out = resp.getWriter();

		try(Connection conn = ds.getConnection()){

			Statement stmt = conn.createStatement();
			stmt.execute(createDbQuery);

			final PreparedStatement createPostStmt = conn.prepareStatement(insertPost,Statement.RETURN_GENERATED_KEYS); 
			createPostStmt.setString(1, post.getTitle());
			createPostStmt.setString(2, post.getAuthor());
			createPostStmt.setString(3, post.getDescription());

			createPostStmt.execute();
			out.println(
					"Article with the title: " + req.getParameter("title") + " by "
							+ req.getParameter("author") + " and the content: "
							+ req.getParameter("description") + " added." + dbUrl);

			
			out.println();
			out.println("List of saved posts:");

			final String listBlogsString = "SELECT id, title, author, description from public.posts";

			PreparedStatement listBlogsStmt = conn.prepareStatement(listBlogsString);

			try (ResultSet rs = listBlogsStmt.executeQuery()) {
				while (rs.next()) {
					BlogPost p = new BlogPost();
					out.println("Post id: " + rs.getInt(1));
					p.setTitle(rs.getString(2));
					p.setAuthor(rs.getString(3));
					p.setDescription(rs.getString(4));
					out.println(p);
					out.println();
				}
			}

			if(conn != null)
				conn.close();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		ds.close();
	}
}
