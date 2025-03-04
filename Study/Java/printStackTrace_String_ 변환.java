StringWriter errors = new StringWriter();
e.printStackTrace(new PrintWriter(errors));
errors.toString();
