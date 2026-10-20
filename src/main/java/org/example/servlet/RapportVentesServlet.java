package org.example.servlet;

import org.example.service.RapportVentesService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/rapport-ventes")
public class RapportVentesServlet extends HttpServlet {
    private final RapportVentesService service = new RapportVentesService();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().println("Rapport des ventes du mois precedent");
        for (RapportVentesService.LigneRapport ligne : service.moisPrecedent())
            response.getWriter().println(ligne);
    }
}
