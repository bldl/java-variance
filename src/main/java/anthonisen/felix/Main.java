package anthonisen.felix;

import com.github.javaparser.utils.Log;

import anthonisen.felix.astParsing.Covariancer;

class Main {
    public static void main(String[] args) {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());
        Covariancer manip = new Covariancer();
        manip.makeCovariant();
    }
}