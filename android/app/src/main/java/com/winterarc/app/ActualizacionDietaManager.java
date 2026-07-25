package com.winterarc.app;

// NOTA DE RECONSTRUCCIÓN: esta clase es referenciada por ActualizacionDietaActivity.java
// (ActualizacionDietaManager.actualizarDieta(pesoActual, pesoObjetivo, caloriasActuales, objetivo))
// pero su código real NO aparece en ningún punto del reporte (se buscó exhaustivamente, incluida
// la sección Swift/iOS). Es la única pieza de lógica de negocio del proyecto que se calcula
// enteramente en el cliente en vez de pedirse al servidor PHP. No se puede "recuperar" ni "portar"
// porque no existe ninguna fuente del cálculo real. Se implementó aplicando ÚNICAMENTE la
// metodología nutricional que el propio reporte documenta en el capítulo 2.2 ("Fundamentos
// nutricionales y de entrenamiento"): TMB + ajuste por objetivo (déficit/superávit/mantenimiento),
// distribución de macronutrientes 40% carbohidratos / 30% proteína / 30% grasa (la distribución
// que el propio proyecto eligió, documentada explícitamente), y las equivalencias 1g proteína=4kcal,
// 1g carbohidrato=4kcal, 1g grasa=9kcal. El porcentaje de déficit/superávit (15%) NO está
// documentado en el reporte con un valor exacto; se usó un valor típico y razonable. Si se
// recupera el proyecto original, ESTA clase es la que con más probabilidad debe reemplazarse.
public class ActualizacionDietaManager {

    private static final double PORCENTAJE_AJUSTE = 0.15;

    public static class DietaActualizada {
        public final int calorias;
        public final int proteinas;
        public final int carbohidratos;
        public final int grasas;

        public DietaActualizada(int calorias, int proteinas, int carbohidratos, int grasas) {
            this.calorias = calorias;
            this.proteinas = proteinas;
            this.carbohidratos = carbohidratos;
            this.grasas = grasas;
        }
    }

    public static DietaActualizada actualizarDieta(
            double pesoActual, double pesoObjetivo, int caloriasActuales, String objetivo) {

        int caloriasNuevas;
        switch (objetivo == null ? "" : objetivo.toLowerCase()) {
            case "definicion":
                caloriasNuevas = (int) Math.round(caloriasActuales * (1 - PORCENTAJE_AJUSTE));
                break;
            case "volumen":
                caloriasNuevas = (int) Math.round(caloriasActuales * (1 + PORCENTAJE_AJUSTE));
                break;
            default:
                caloriasNuevas = caloriasActuales;
        }

        // Distribución 40% carbohidratos / 30% proteína / 30% grasa (capítulo 2.2 del reporte),
        // convertida a gramos con 4 kcal/g (proteína y carbohidrato) y 9 kcal/g (grasa).
        int proteinas = (int) Math.round((caloriasNuevas * 0.30) / 4);
        int carbohidratos = (int) Math.round((caloriasNuevas * 0.40) / 4);
        int grasas = (int) Math.round((caloriasNuevas * 0.30) / 9);

        return new DietaActualizada(caloriasNuevas, proteinas, carbohidratos, grasas);
    }
}
