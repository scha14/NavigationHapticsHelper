<?php
exec("gpio mode 2 out");
exec("gpio mode 3 out");
if (isset($_GET['led1'])) {
		exec("gpio write 2 1");
		exec("sleep 2");
		exec("gpio write 2 0");
}
if(isset($_GET['led2'])) {
    	exec("gpio write 3 1");
    	exec("sleep 2");
    	exec("gpio write 3 0");
}

if(isset($_GET['led3'])) {
		exec("gpio write 3 1");
		exec("gpio write 2 1");
        exec("sleep 2");
        exec("gpio write 3 0");
        exec("gpio write 2 0");
}
    exec("gpio write 3 0");
    exec("gpio write 2 0");
?>