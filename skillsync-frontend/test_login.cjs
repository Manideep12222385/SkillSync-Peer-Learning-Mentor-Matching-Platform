const axios = require('axios');

async function test() {
  try {
    const res = await axios.post('http://localhost:8085/auth-service/auth/login', {
      email: "Nandu@gmail.com",
      password: "Nandu@0369"
    });
    console.log("SUCCESS:", res.data);
  } catch (err) {
    console.log("ERROR:", err.response?.status, err.response?.data);
  }
}

test();
