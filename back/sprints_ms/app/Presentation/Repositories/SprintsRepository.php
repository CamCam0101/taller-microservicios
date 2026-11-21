<?php

namespace App\Presentation\Repositories;

use App\Controllers\SprintsController;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class SprintsRepository
{
    public function get_all_sprints(Request $req, Response $resp, $args): Response
    {
        $controller = new SprintsController();
        $rows = $controller->all();
        if (count($rows) == 0) {
            return $resp->withStatus(204);
        } else {
            $resp->getBody()->write($rows->toJson());
            return $resp
                ->withStatus(200)
                ->withHeader('Content-Type', 'application/json');
        }
    }

    public function get_latest_sprint(Request $req, Response $resp, $args): Response
    {
        $controller = new SprintsController();
        $row = $controller->find_latest();
        if (empty($row)) {
            return $resp->withStatus(204);
        } else {
            $resp->getBody()->write($row->toJson());
            return $resp
                ->withStatus(200)
                ->withHeader('Content-Type', 'application/json');
        }
    }

    public function get_sprint(Request $req, Response $resp, $args): Response
    {
        $id = (int) $args["id"];
        $controller = new SprintsController();
        $row = $controller->find_sprint_by_id($id);
        if (empty($row)) {
            return $resp->withStatus(404);
        } else {
            $resp->getBody()->write($row->toJson());
            return $resp
                ->withStatus(200)
                ->withHeader('Content-Type', 'application/json');
        }
    }

    public function create_sprint(Request $req, Response $resp, $args): Response
    {
        $controller = new SprintsController();
        $body = $req->getBody()->getContents();
        $data = json_decode($body, true);

        if (empty($data['nombre']) || empty($data['fecha_inicio']) || empty($data['fecha_fin'])) {
            return $resp->withStatus(400);
        }

        $row = $controller->create($data);
        $resp->getBody()->write($row->toJson());
        return $resp
            ->withStatus(201)
            ->withHeader('Content-Type', 'application/json');
    }

    public function update_sprint(Request $req, Response $resp, $args): Response
    {
        $id = (int) $args["id"];
        $body = $req->getBody()->getContents();
        $data = json_decode($body, true);

        $controller = new SprintsController();
        $row = $controller->update($id, $data ?? []);
        if (empty($row)) {
            return $resp->withStatus(404);
        } else {
            $resp->getBody()->write($row->toJson());
            return $resp
                ->withStatus(200)
                ->withHeader('Content-Type', 'application/json');
        }
    }

    public function delete_sprint(Request $req, Response $resp, $args): Response
    {
        $id = (int) $args["id"];
        $controller = new SprintsController();
        $row = $controller->delete($id);
        if (empty($row)) {
            return $resp->withStatus(404);
        } else {
            return $resp->withStatus(200);
        }
    }
}