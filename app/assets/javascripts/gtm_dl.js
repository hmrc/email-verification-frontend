//push any data-gtag objects in the format "key:value, key:value" into global dataLayer
(function (d, w) {

	function ready(fn) {
		if (d.readyState !== 'loading') {
			fn();
		} else {
			d.addEventListener('DOMContentLoaded', fn);
		}
	}

	function parseData(string) {
		var properties = string.split(', ');
		var obj = {};
		properties.forEach(function(property) {
			var tup = property.split(':');
			obj[tup[0]] = tup[1]
		});
		return obj
	}

	ready(function() {
		w.dataLayer = w.dataLayer || [];
		var localData = d.querySelectorAll('[data-gtag]');
		var localObj = {
			'event': 'DOMContentLoaded',
			'Session ID': new Date().getTime() + '.' + Math.random().toString(36).substring(5),
			'Hit TimeStamp': new Date().toUTCString()
		};
		Array.prototype.forEach.call(localData, function (el, i) {
			localObj = Object.assign( localObj, parseData(el.getAttribute('data-gtag')) )
		});

		w.dataLayer.push(localObj);
	})

})(document,window);
