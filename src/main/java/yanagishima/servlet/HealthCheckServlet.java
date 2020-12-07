package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class HealthCheckServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final TinyORM db;

    @Inject
    public HealthCheckServlet(TinyORM db) {
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        db.executeQuery("select 1");
    }

}
